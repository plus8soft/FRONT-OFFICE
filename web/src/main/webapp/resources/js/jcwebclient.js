/*!
 * \file JCWebClient.js
 */

/** @name 1. JavaScript SDK.
 **/
///@{

var json_parse = (function () {
    "use strict";

    // This function creates a JSON parse function that uses a state machine rather
    // than the dangerous eval function to parse a JSON text.

    var state,      // The state of the parser, one of
        // 'go'         The starting state
        // 'ok'         The final, accepting state
        // 'firstokey'  Ready for the first key of the object or
        //              the closing of an empty object
        // 'okey'       Ready for the next key of the object
        // 'colon'      Ready for the colon
        // 'ovalue'     Ready for the value half of a key/value pair
        // 'ocomma'     Ready for a comma or closing }
        // 'firstavalue' Ready for the first value of an array or
        //              an empty array
        // 'avalue'     Ready for the next value of an array
        // 'acomma'     Ready for a comma or closing ]
        stack,      // The stack, for controlling nesting.
        container,  // The current container object or array
        key,        // The current key
        value,      // The current value
        escapes = { // Escapement translation table
            '\\': '\\',
            '"': '"',
            '/': '/',
            't': '\t',
            'n': '\n',
            'r': '\r',
            'f': '\f',
            'b': '\b'
        },
        string = {   // The actions for string tokens
            go: function () {
                state = 'ok';
            },
            firstokey: function () {
                key = value;
                state = 'colon';
            },
            okey: function () {
                key = value;
                state = 'colon';
            },
            ovalue: function () {
                state = 'ocomma';
            },
            firstavalue: function () {
                state = 'acomma';
            },
            avalue: function () {
                state = 'acomma';
            }
        },
        number = {   // The actions for number tokens
            go: function () {
                state = 'ok';
            },
            ovalue: function () {
                state = 'ocomma';
            },
            firstavalue: function () {
                state = 'acomma';
            },
            avalue: function () {
                state = 'acomma';
            }
        },
        action = {

            // The action table describes the behavior of the machine. It contains an
            // object for each token. Each object contains a method that is called when
            // a token is matched in a state. An object will lack a method for illegal
            // states.

            '{': {
                go: function () {
                    stack.push({state: 'ok'});
                    container = {};
                    state = 'firstokey';
                },
                ovalue: function () {
                    stack.push({container: container, state: 'ocomma', key: key});
                    container = {};
                    state = 'firstokey';
                },
                firstavalue: function () {
                    stack.push({container: container, state: 'acomma'});
                    container = {};
                    state = 'firstokey';
                },
                avalue: function () {
                    stack.push({container: container, state: 'acomma'});
                    container = {};
                    state = 'firstokey';
                }
            },
            '}': {
                firstokey: function () {
                    var pop = stack.pop();
                    value = container;
                    container = pop.container;
                    key = pop.key;
                    state = pop.state;
                },
                ocomma: function () {
                    var pop = stack.pop();
                    container[key] = value;
                    value = container;
                    container = pop.container;
                    key = pop.key;
                    state = pop.state;
                }
            },
            '[': {
                go: function () {
                    stack.push({state: 'ok'});
                    container = [];
                    state = 'firstavalue';
                },
                ovalue: function () {
                    stack.push({container: container, state: 'ocomma', key: key});
                    container = [];
                    state = 'firstavalue';
                },
                firstavalue: function () {
                    stack.push({container: container, state: 'acomma'});
                    container = [];
                    state = 'firstavalue';
                },
                avalue: function () {
                    stack.push({container: container, state: 'acomma'});
                    container = [];
                    state = 'firstavalue';
                }
            },
            ']': {
                firstavalue: function () {
                    var pop = stack.pop();
                    value = container;
                    container = pop.container;
                    key = pop.key;
                    state = pop.state;
                },
                acomma: function () {
                    var pop = stack.pop();
                    container.push(value);
                    value = container;
                    container = pop.container;
                    key = pop.key;
                    state = pop.state;
                }
            },
            ':': {
                colon: function () {
                    if (Object.hasOwnProperty.call(container, key)) {
                        throw new SyntaxError('Duplicate key "' + key + '"');
                    }
                    state = 'ovalue';
                }
            },
            ',': {
                ocomma: function () {
                    container[key] = value;
                    state = 'okey';
                },
                acomma: function () {
                    container.push(value);
                    state = 'avalue';
                }
            },
            'true': {
                go: function () {
                    value = true;
                    state = 'ok';
                },
                ovalue: function () {
                    value = true;
                    state = 'ocomma';
                },
                firstavalue: function () {
                    value = true;
                    state = 'acomma';
                },
                avalue: function () {
                    value = true;
                    state = 'acomma';
                }
            },
            'false': {
                go: function () {
                    value = false;
                    state = 'ok';
                },
                ovalue: function () {
                    value = false;
                    state = 'ocomma';
                },
                firstavalue: function () {
                    value = false;
                    state = 'acomma';
                },
                avalue: function () {
                    value = false;
                    state = 'acomma';
                }
            },
            'null': {
                go: function () {
                    value = null;
                    state = 'ok';
                },
                ovalue: function () {
                    value = null;
                    state = 'ocomma';
                },
                firstavalue: function () {
                    value = null;
                    state = 'acomma';
                },
                avalue: function () {
                    value = null;
                    state = 'acomma';
                }
            }
        },
        source;

    function debackslashify(text) {

        // Remove and replace any backslash escapement.

        return text.replace(/\\(?:u(.{4})|([^u]))/g, function (ignore, b, c) {
            return b
                ? String.fromCharCode(parseInt(b, 16))
                : escapes[c];
        });
    }

    return {
        init: function () {
            // Set the starting state.
            source = '';
            state = 'go';
            // The stack records the container, key, and state for each object or array
            // that contains another object or array while processing nested structures.
            stack = [];
        },
        wellformed: function () {
            // The parsing is finished. If we are not in the final 'ok' state, or if the
            // remaining source contains anything except whitespace, then we did not have
            //a well-formed JSON text.
            if (state === 'ok' && !(/[^\u0020\t\n\r]/.test(source)))
                return true;
            else
                return false;
            //throw state instanceof SyntaxError
            //    ? state
            //    : new SyntaxError('JSON');
        },
        result: function (reviver) {

            // If there is a reviver function, we recursively walk the new structure,
            // passing each name/value pair to the reviver function for possible
            // transformation, starting with a temporary root object that holds the current
            // value in an empty key. If there is not a reviver function, we simply return
            // that value.

            return typeof reviver === 'function'
                ? (function walk(holder, key) {
                    var k, v, value = holder[key];
                    if (value && typeof value === 'object') {
                        for (k in value) {
                            if (Object.prototype.hasOwnProperty.call(value, k)) {
                                v = walk(value, k);
                                if (v !== undefined) {
                                    value[k] = v;
                                } else {
                                    delete value[k];
                                }
                            }
                        }
                    }
                    return reviver.call(holder, key, value);
                }({'': value}, ''))
                : value;

        },
        parse: function (pieceOfJSON) {

            // A regular expression is used to extract tokens from the JSON text.
            // The extraction process is cautious.

            var result,
                tx = /^[\u0020\t\n\r]*(?:([,:\[\]{}]|true|false|null)|(-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)|"((?:[^\r\n\t\\\"]|\\(?:["\\\/trnfb]|u[0-9a-fA-F]{4}))*)")/;

            //  tx = /^[\u0020\t\n\r]* // white space
            //      (?:
            //          ([,:\[\]{}] | true | false | null) |        // , : [ ] { } true false null
            //          (-?\d+ (?:\.\d*)? (?:[eE][+\-]?\d+)? ) |    // -123.456E-78
            //          "(                                                              // " 
            //               (?:[^\r\n\t\\\"] | \\(?:["\\\/trnfb] | u[0-9a-fA-F]{4}) )* // white space \ " \u0035
            //          )"                                                              // "
            //      )/;

            // If any error occurs, we will catch it and go not wellformed.
            source += pieceOfJSON;

            try {

                // For each token...
                while (true) {
                    result = tx.exec(source);
                    if (!result) {
                        break;
                    }

                    // result is the result array from matching the tokenizing regular expression.
                    //  result[0] contains everything that matched, including any initial whitespace.
                    //  result[1] contains any punctuation that was matched, or true, false, or null.
                    //  result[2] contains a matched number, still in string form.
                    //  result[3] contains a matched string, without quotes but with escapement.

                    if (result[1]) {

                        // Token: Execute the action for this state and token.

                        action[result[1]][state]();

                    } else if (result[2]) {

                        // Number token: Convert the number string into a number value and execute
                        // the action for this state and number.

                        value = +result[2];
                        number[state]();
                    } else {

                        // String token: Replace the escapement sequences and execute the action for
                        // this state and string.

                        value = debackslashify(result[3]);
                        string[state]();
                    }

                    // Remove the token from the string. The loop will continue as long as there
                    // are tokens. This is a slow process, but it allows the use of ^ matching,
                    // which assures that no illegal tokens slip through.

                    source = source.slice(result[0].length);
                }

                // If we find a state/token combination that is illegal, then the action will
                // cause an error. We handle the error by simply changing the state.

            } catch (e) {
                state = e;
            }
        }
    };
}());

/*!
 * \class JCWebClient
 * \brief JC-WebClient SDK
 */

//@cond DUMMY

var JCWebClient = (function () {

    var _JCWebClient_Static;

    _JCWebClient_Static = new Object;

    // Request URL
    var thisPageUrl = window.location.href;
    var urlParts = thisPageUrl.split("//");
    if (urlParts[0] == 'file:')
        _JCWebClient_Static.requestUrl = "https://localhost:24738/jcext?";
    else
        _JCWebClient_Static.requestUrl = urlParts[0] + "//localhost:24738/jcext?";

    _JCWebClient_Static.saveSession = true;
    _JCWebClient_Static.initialize = initialize;

    // Internal API functions
    _JCWebClient_Static.getAllTokens = getAllTokens;
    _JCWebClient_Static.getAllSlots = getAllSlots;
    _JCWebClient_Static.getTokenInfo = getTokenInfo;
    _JCWebClient_Static.getSlotInfo = getSlotInfo;
    _JCWebClient_Static.getLoggedInState = getLoggedInState;
    _JCWebClient_Static.getPluginVersion = getPluginVersion;
    _JCWebClient_Static.checkWebBrowserVersion = checkWebBrowserVersion;
    _JCWebClient_Static.initToken = initToken;
    _JCWebClient_Static.initTokenWithoutUserPIN = initTokenWithoutUserPIN;
    _JCWebClient_Static.initTokenUI = initTokenUI;
    _JCWebClient_Static.getSystemInfo = getSystemInfo;
    _JCWebClient_Static.getSystemInfoAsync = getSystemInfoAsync;
    // Error handling
    _JCWebClient_Static.getLastError = getLastError;
    _JCWebClient_Static.getErrorMessage = getErrorMessage;
    _JCWebClient_Static.isAsyncOperationInProgress = isAsyncOperationInProgress;
    // PIN code operations
    _JCWebClient_Static.bindToken = bindToken;
    _JCWebClient_Static.bindTokenAsync = bindTokenAsync;
    _JCWebClient_Static.bindTokenUI = bindTokenUI;
    _JCWebClient_Static.unbindToken = unbindToken;
    _JCWebClient_Static.changePIN = changePIN;
    _JCWebClient_Static.changePINasync = changePINAsync;
    _JCWebClient_Static.changePINAsync = changePINAsync;
    _JCWebClient_Static.changePINUI = changePINUI;
    _JCWebClient_Static.changePINUIAsynch = changePINUIAsync;
    _JCWebClient_Static.changePINUIAsync = changePINUIAsync;
    _JCWebClient_Static.initUserPIN = initUserPIN;
    _JCWebClient_Static.initUserPINasync = initUserPINAsync;
    _JCWebClient_Static.initUserPINAsync = initUserPINAsync;
    _JCWebClient_Static.initUserPINUI = initUserPINUI;
    _JCWebClient_Static.initUserPINUIAsynch = initUserPINUIAsync;
    _JCWebClient_Static.initUserPINUIAsync = initUserPINUIAsync;
    _JCWebClient_Static.initUserPINAFT = initUserPINAFT;
    _JCWebClient_Static.unblockUserPIN = unblockUserPIN;
    _JCWebClient_Static.unblockUserPINasync = unblockUserPINasync;
    _JCWebClient_Static.unblockUserPINUI = unblockUserPINUI;
    _JCWebClient_Static.unblockUserPINUIAsynch = unblockUserPINUIAsync;
    _JCWebClient_Static.unblockUserPINUIAsync = unblockUserPINUIAsync;
    // Container operations
    _JCWebClient_Static.createContainer = createContainer;
    _JCWebClient_Static.createContainerEx = createContainerEx;
    _JCWebClient_Static.createContainerAsync = createContainerAsync;
    _JCWebClient_Static.createContainerExAsync = createContainerExAsync;
    _JCWebClient_Static.deleteContainerOrCertificate = deleteContainerOrCertificate;
    _JCWebClient_Static.deleteCertificate = deleteCertificate;
    _JCWebClient_Static.changeContainerDescription = changeContainerDescription;
    // PKI integration
    _JCWebClient_Static.genCSR = genCSR;
    _JCWebClient_Static.genCSRAsync = genCSRAsync;
    _JCWebClient_Static.genCSRUsingHardwareHash = genCSRUsingHardwareHash;
    _JCWebClient_Static.genCSRUsingHardwareHashAsync = genCSRUsingHardwareHashAsync;
    _JCWebClient_Static.writeSelfSignedCertificate = writeSelfSignedCertificate;
    _JCWebClient_Static.writeCertificate = writeCertificate;
    _JCWebClient_Static.writeCertificateAsync = writeCertificateAsync;
    _JCWebClient_Static.writeSignerCertificate = writeSignerCertificate;
    _JCWebClient_Static.writeSignerCertificateAsync = writeSignerCertificateAsync;
    _JCWebClient_Static.deleteSignerCertificateById = deleteSignerCertificateById;
    _JCWebClient_Static.deleteAllSignerCertificatesForId = deleteAllSignerCertificatesForId;
    _JCWebClient_Static.deleteAllSignerCertificates = deleteAllSignerCertificates;
    _JCWebClient_Static.readCertificate = readCertificate;
    _JCWebClient_Static.readCertificateEx = readCertificateEx;
    _JCWebClient_Static.readSignerCertificateList = readSignerCertificateList;
    _JCWebClient_Static.readSignerCertificateListAsync = readSignerCertificateListAsync;
    _JCWebClient_Static.writeServerPublicKey = writeServerPublicKey;
    _JCWebClient_Static.readServerPublicKey = readServerPublicKey;
    _JCWebClient_Static.writeServerCertificate = writeServerCertificate;
    _JCWebClient_Static.readServerCertificate = readServerCertificate;
    _JCWebClient_Static.readCkaID = readCkaID;
    _JCWebClient_Static.writeCkaID = writeCkaID;
    _JCWebClient_Static.readPublicKey = readPublicKey;
    _JCWebClient_Static.getCertificateInfo = getCertificateInfo;
    _JCWebClient_Static.getCertificatePublicKey = getCertificatePublicKey;
    _JCWebClient_Static.getCertificateInfoEx = getCertificateInfoEx;
    _JCWebClient_Static.getContainerList = getContainerList;
    _JCWebClient_Static.parseCertificateEx = parseCertificateEx;
    _JCWebClient_Static.parseCertificate = parseCertificate;
    _JCWebClient_Static.parseX509CertificateEx = parseX509CertificateEx;
    _JCWebClient_Static.parseX509Certificate = parseX509Certificate;
    _JCWebClient_Static.getCertificateList = getCertificateList;
    _JCWebClient_Static.getCertificateListAsync = getCertificateListAsync;
    _JCWebClient_Static.getCertificateListEx = getCertificateListEx;
    _JCWebClient_Static.getCertificateListExAsync = getCertificateListExAsync;
    _JCWebClient_Static.getSignerCertificateList = getSignerCertificateList;
    _JCWebClient_Static.getSignerCertificateListAsync = getSignerCertificateListAsync;
    _JCWebClient_Static.getAllValidCertificateChains = getAllValidCertificateChains;
    _JCWebClient_Static.getAllValidCertificateChainsAsync = getAllValidCertificateChainsAsync;
    _JCWebClient_Static.getAllInvalidCertificateChains = getAllInvalidCertificateChains;
    _JCWebClient_Static.getAllInvalidCertificateChainsAsync = getAllInvalidCertificateChainsAsync;
    _JCWebClient_Static.getAllUnusedCertificateChains = getAllUnusedCertificateChains;
    _JCWebClient_Static.getAllUnusedCertificateChainsAsync = getAllUnusedCertificateChainsAsync;
    _JCWebClient_Static.getAllCertificateChains = getAllCertificateChains;
    _JCWebClient_Static.getAllCertificateChainsAsync = getAllCertificateChainsAsync;
    _JCWebClient_Static.createStandaloneCertificate = createStandaloneCertificate;
    _JCWebClient_Static.verifyCertificateChain = verifyCertificateChain;
    _JCWebClient_Static.verifyCertificateChainAsync = verifyCertificateChainAsync;
    _JCWebClient_Static.verifyCertificateChainEx = verifyCertificateChainEx;
    _JCWebClient_Static.verifyCertificateChainExAsync = verifyCertificateChainExAsync;
    _JCWebClient_Static.verifyCertificateChainExExternalTrustedCerts = verifyCertificateChainExExternalTrustedCerts;
    _JCWebClient_Static.verifyCertificateChainExExternalTrustedCertsAsync = verifyCertificateChainExExternalTrustedCertsAsync;
    // Signing and signature verification
    _JCWebClient_Static.enableReverifyPINOnSignature = enableReverifyPINOnSignature;
    _JCWebClient_Static.isReverifyPINOnSignatureEnabled = isReverifyPINOnSignatureEnabled;
    _JCWebClient_Static.signData = signData;
    _JCWebClient_Static.signDataAsync = signDataAsync;
    _JCWebClient_Static.signDataInFile = signDataInFile;
    _JCWebClient_Static.signDataInFileBase64 = signDataInFileBase64;
    _JCWebClient_Static.signDataUsingHardwareHashBase64 = signDataUsingHardwareHashBase64;
    _JCWebClient_Static.signDataInFileUsingHardwareHash = signDataInFileUsingHardwareHash;
    _JCWebClient_Static.signDataInFileUsingHardwareHashBase64 = signDataInFileUsingHardwareHashBase64;
    _JCWebClient_Static.signBase64EncodedData = signBase64EncodedData;
    _JCWebClient_Static.signBase64EncodedDataAsync = signBase64EncodedDataAsync;
    _JCWebClient_Static.signDataUsingHardwareHash = signDataUsingHardwareHash;
    _JCWebClient_Static.signBase64EncodedDataUsingHardwareHash = signBase64EncodedDataUsingHardwareHash;
    _JCWebClient_Static.signHash = signHash;
    _JCWebClient_Static.signDataInByte = signDataInByte;
    _JCWebClient_Static.signHashInByte = signHashInByte;
    _JCWebClient_Static.signDataInByteAsync = signDataInByteAsync;
    _JCWebClient_Static.signHashInByteAsync = signHashInByteAsync;
    _JCWebClient_Static.verifyData = verifyData;
    _JCWebClient_Static.verifyDataHW = verifyDataHW;
    _JCWebClient_Static.verifyDataHWUsingHardwareHash = verifyDataHWUsingHardwareHash;
    _JCWebClient_Static.verifyBase64EncodedData = verifyBase64EncodedData;
    _JCWebClient_Static.verifyBase64EncodedDataHW = verifyBase64EncodedDataHW;
    _JCWebClient_Static.verifyBase64EncodedDataHWUsingHardwareHash = verifyBase64EncodedDataHWUsingHardwareHash;
    _JCWebClient_Static.verifyRawBase64EncodedData = verifyRawBase64EncodedData;
    _JCWebClient_Static.verifyRawBase64EncodedDataEx = verifyRawBase64EncodedDataEx;
    _JCWebClient_Static.verifyDataInFile = verifyDataInFile;
    _JCWebClient_Static.verifyDataInByte = verifyDataInByte;
    _JCWebClient_Static.verifyHashInByte = verifyHashInByte;
    _JCWebClient_Static.digest = digest;
    _JCWebClient_Static.digestAsync = digestAsync;
    _JCWebClient_Static.digestHardware = digestHardware;
    _JCWebClient_Static.digestHardwareAsync = digestHardwareAsync;
    _JCWebClient_Static.digestNoAuth = digestNoAuth;
    _JCWebClient_Static.digestNoAuthAsync = digestNoAuthAsync;
    _JCWebClient_Static.digestNoAuthHardware = digestNoAuthHardware;
    _JCWebClient_Static.digestNoAuthHardwareAsync = digestNoAuthHardwareAsync;
    _JCWebClient_Static.pkcs7Parse = pkcs7Parse;
    _JCWebClient_Static.pkcs7ParseBase64Encoded = pkcs7ParseBase64Encoded;
    _JCWebClient_Static.pkcs7ParseInFile = pkcs7ParseInFile;
    _JCWebClient_Static.pkcs7ParseInFileBase64Encoded = pkcs7ParseInFileBase64Encoded;
    _JCWebClient_Static.makeContainerPkcs21Ready = makeContainerPkcs21Ready;
    // Authentication and channel protection
    _JCWebClient_Static.establishSChannelBegin = establishSChannelBegin;
    _JCWebClient_Static.establishSChannelContinue = establishSChannelContinue;
    _JCWebClient_Static.unilateralAuthenticationBegin = unilateralAuthenticationBegin;
    _JCWebClient_Static.unilateralAuthenticationContinue = unilateralAuthenticationContinue;
    _JCWebClient_Static.encode = encode;
    _JCWebClient_Static.decode = decode;
    // Data storage
    _JCWebClient_Static.storeBinaryData = storeBinaryData;
    _JCWebClient_Static.storePrivateBinaryData = storePrivateBinaryData;
    _JCWebClient_Static.getBinaryDataObjectList = getBinaryDataObjectList;
    _JCWebClient_Static.readBinaryObject = readBinaryObject;
    _JCWebClient_Static.deleteBinaryObject = deleteBinaryObject;
    _JCWebClient_Static.modifyBinaryObject = modifyBinaryObject;
    // Antifraud terminal and SWYX technology methods
    _JCWebClient_Static.isSwyxReader = isSwyxReader;
    _JCWebClient_Static.enroll = enroll;
    _JCWebClient_Static.performPersonalization = performPersonalization;
    _JCWebClient_Static.performPersonalizationAsync = performPersonalizationAsync;
    _JCWebClient_Static.getReaderAppletSerialNumber = getReaderAppletSerialNumber;
    _JCWebClient_Static.swyxStart = swyxStart;
    _JCWebClient_Static.swyxStartEx = swyxStartEx;
    _JCWebClient_Static.swyxStartAsync = swyxStartAsync;
    _JCWebClient_Static.swyxStartExAsync = swyxStartExAsync;
    _JCWebClient_Static.swyxStop = swyxStop;
    _JCWebClient_Static.swyxStopAsync = swyxStopAsync;
    _JCWebClient_Static.swyxDisplay = swyxDisplay;
    _JCWebClient_Static.swyxDisplayEx = swyxDisplayEx;
    _JCWebClient_Static.swyxDisplayAsync = swyxDisplayAsync;
    _JCWebClient_Static.swyxDisplayExAsync = swyxDisplayExAsync;
    _JCWebClient_Static.swyxSign = swyxSign;
    _JCWebClient_Static.swyxSignAsync = swyxSignAsync;
    _JCWebClient_Static.swyxSignEx = swyxSignEx;
    _JCWebClient_Static.swyxSignExAsync = swyxSignExAsync;
    _JCWebClient_Static.aftEnterAdminPINAsync = aftEnterAdminPINAsync;
    _JCWebClient_Static.aftSaveAdminPINAsync = aftSaveAdminPINAsync;
    _JCWebClient_Static.aftInitCardAsync = aftInitCardAsync;
    _JCWebClient_Static.aftInitUserPINAsync = aftInitUserPINAsync;
    _JCWebClient_Static.bindTokenAFTAsync = bindTokenAFTAsync;
    _JCWebClient_Static.aftGetReaderVersion = aftGetReaderVersion;
    _JCWebClient_Static.aftCardlessSupport = aftCardlessSupport;
    _JCWebClient_Static.aftGetPINAsync = aftGetPINAsync;
    _JCWebClient_Static.aftGetNewPINAsync = aftGetNewPINAsync;
    _JCWebClient_Static.aftUpdateFirmwareAsync = aftUpdateFirmwareAsync;
    _JCWebClient_Static.aftGetBrokenReaders = aftGetBrokenReaders;
    _JCWebClient_Static.aftFixBrokenReaderAsync = aftFixBrokenReaderAsync;
    // Async notifications for smartcard events
    _JCWebClient_Static.TokenAddedSubscriptions = [];
    _JCWebClient_Static.TokenRemovedSubscriptions = [];
    _JCWebClient_Static.SmartCardAddedSubscriptions = [];
    _JCWebClient_Static.SmartCardRemovedSubscriptions = [];
    _JCWebClient_Static.LoginStateChangedSubscriptions = [];
    _JCWebClient_Static.addEventListener = addEventListener;
    _JCWebClient_Static.attachEvent = attachEvent;
    _JCWebClient_Static.removeEventListener = removeEventListener;
    _JCWebClient_Static.detachEvent = detachEvent;
    // Must pass check: typeof (JCWebClient) == 'undefined' || JCWebClient().valid == null
    _JCWebClient_Static.valid = true;
    _JCWebClient_Static.session_id = "";

    // PKCS11 result
    _JCWebClient_Static.SCLayerResponce = null;
    // User callback

    _JCWebClient_Static.callback = null;
    _JCWebClient_Static.asyncRequestIsOngoing = false;

    // Debug
    _JCWebClient_Static.debugFunction = debugFunction;
    _JCWebClient_Static.debugFunctionAsynch = debugFunctionAsynch;
    // Web session cleanup
    _JCWebClient_Static.closeWebSession = closeWebSession;

    // Timeout ID for event request response check
    _JCWebClient_Static.eventRetriveCheckTimeoutId = null;

    _JCWebClient_Static.SystemInfoClass = {
        guid: "guid",
        user: "user",
        osType: "osType",
        osDescription: "osDescription",
        osArchitecture: "osArchitecture"
    };


//@endcond


    /*!
     * \fn loadSessionID()
     * \memberof JCWebClient
     * \brief Start working with JCWebClient() context.
     *
     * This method must be called at the start of the script on the web page.
     * Use JCWebClient().navigateTo(..., false) to re-initialize context on the new page.
     */
    function loadSessionID() {
        var ret = "";

        if (sessionStorage) {
            ret = sessionStorage.getItem("jc-session-id");

            if (ret == null || sessionStorage.getItem("jc-session-id-in-use")) {
                ret = String(Math.floor(Math.random() * 4000000000));
            }

            sessionStorage.setItem("jc-session-id", ret);
            sessionStorage.setItem("jc-session-id-in-use", true);
        }

        _JCWebClient_Static.session_id = ret;
    }

    return function (callback) {
        if (callback) {
            _JCWebClient_Static.callback = callback;
        }
        // When JCWebClient() is called from JSONP callback it is not yet defined
        return _JCWebClient_Static;
    }

    function initialize() {
        // Load Web session ID from sessionStore
        loadSessionID();
        // If async (e.g. JCWebClient().RetrieveEvents()), after TLS connection Firefox may hang
        // So Ping() request is used to "wake" FF and proceed to subsequent ajax (getPluginVersion etc.)
        Ping();
        // If JCWebClient 3.0, start async handling of eToken events
        RetrieveEvents();

        attachWindowUnloadEvent();
    }

    function attachWindowUnloadEvent() {

        var pageUnloaded = false;

        function onPageUnload() {
            if (pageUnloaded) {
                return;
            }
            pageUnloaded = true;

            if (sessionStorage) {
                sessionStorage.removeItem("jc-session-id-in-use");
            }

            if (_JCWebClient_Static.saveSession && sessionStorage) {
                sessionStorage.setItem("jc-session-id", _JCWebClient_Static.session_id);
            } else {
                // Default behavior
                _JCWebClient_Static.closeWebSession();
            }
        }

        window.onbeforeunload = onPageUnload;
        window.onunload = onPageUnload;
    }

    function constructTypedResponse(toBeTypedResponse) {
        var prefBlob = "blob:";

        var objectType = Object.prototype.toString.call(toBeTypedResponse);
        if (objectType == '[object Object]') {
            for (prop in toBeTypedResponse) {
                prop = constructTypedResponse(prop);
            }
        } else if (objectType == '[object Array]') {
            var arrRes = [];

            for (var idx = 0; idx < toBeTypedResponse.length; idx++) {

                var isBlob = false;
                var subType = Object.prototype.toString.call(toBeTypedResponse[idx]);
                if (subType == '[object String]') {
                    if (toBeTypedResponse[idx].indexOf(prefBlob) == 0) {
                        isBlob = true;
                    }
                }

                if (isBlob) {
                    toBeTypedResponse[idx] = constructTypedResponse(toBeTypedResponse[idx]);
                    for (var i = 0; i < toBeTypedResponse[idx].length; i++) {
                        arrRes.push(toBeTypedResponse[idx][i]);
                    }
                }
                else {
                    arrRes.push(constructTypedResponse(toBeTypedResponse[idx]));
                }
            }

            toBeTypedResponse = arrRes;
        } else if (objectType == '[object String]') {
            var prefStr = "str:";
            var prefBool = "bool:";
            var prefInt = "int:";
            var prefCkb = "ckb:";
            var prefCkul = "ckul:";
            if (toBeTypedResponse.indexOf(prefStr) == 0) {
                var unprefixedValue = toBeTypedResponse.substring(prefStr.length);
                toBeTypedResponse = unprefixedValue;
            } else if (toBeTypedResponse.indexOf(prefBool) == 0) {
                var strValue = toBeTypedResponse.substring(prefBool.length);
                if (strValue == "true") {
                    toBeTypedResponse = true;
                } else {
                    toBeTypedResponse = false;
                }
            } else if (toBeTypedResponse.indexOf(prefInt) == 0) {
                var strValue = toBeTypedResponse.substring(prefInt.length);
                toBeTypedResponse = parseInt(strValue);
            } else if (toBeTypedResponse.indexOf(prefCkb) == 0) {
                var strValue = toBeTypedResponse.substring(prefCkb.length);
                toBeTypedResponse = parseInt(strValue) % 256;
            } else if (toBeTypedResponse.indexOf(prefCkul) == 0) {
                var strValue = toBeTypedResponse.substring(prefCkul.length);
                toBeTypedResponse = parseInt(strValue);
            } else if (toBeTypedResponse.indexOf(prefBlob) == 0) {
                var strValue = toBeTypedResponse.substring(prefBlob.length);
                var arrNewNode = new Array;
                for (i = 0; toBeTypedResponse.length - prefBlob.length > i; i += 2) {
                    arrNewNode[i / 2] = parseInt("0x" + toBeTypedResponse.substring(prefBlob.length + i, prefBlob.length + i + 2)) % 256;
                }
                toBeTypedResponse = arrNewNode;
            }
        }
        return toBeTypedResponse;
    }

    function requestJcExtFunction2(RequestObj, ticket_id) {
        _JCWebClient_Static.SCLayerResponce = null;

        RequestObj.session_id = _JCWebClient_Static.session_id;
        if (null != ticket_id) {
            RequestObj.ticket_id = ticket_id;
        }
        else {
            RequestObj.ticket_id = String(Math.floor(Math.random() * 4000000000));
        }

        var jsonRequest = JSON.stringify(RequestObj);

        var xhr = new XMLHttpRequest;

        for (var bKeepTrying = true; bKeepTrying;) {
            try {
                xhr.open('POST', _JCWebClient_Static.requestUrl, false);
                xhr.send(jsonRequest);
                if (xhr.status == 200) {
                    bKeepTrying = false;
                }
            }
            catch (exc) {
                xhr.abort();
            }
        }

        if ('[GET]' == xhr.responseText.substring(0, 5)) {
            json_parse.init();

            var parsed = 0;
            do {
                json_parse.parse(xhr.responseText.substring(5));
                parsed += xhr.responseText.length - 5; // subtructing [GET] length

                for (var bKeepTryingAgain = true; bKeepTryingAgain;) {
                    try {
                        xhr.open('GET', _JCWebClient_Static.requestUrl + "session_id=" + _JCWebClient_Static.session_id + "&get_position=" + parsed, false);
                        xhr.send();
                        if (xhr.status == 200) {
                            bKeepTryingAgain = false;
                        }
                    }
                    catch (exc) {
                        xhr.abort();
                    }
                }
            } while ('[GET]' == xhr.responseText.substring(0, 5));

            json_parse.parse(xhr.responseText);

            var responceEdning = xhr.responseText.substring(xhr.responseText.length - 32, xhr.responseText.length);

            if (json_parse.wellformed())
                _JCWebClient_Static.SCLayerResponce = json_parse.result();
            else
                throw new SyntaxError('JSON');
        }
        else
            _JCWebClient_Static.SCLayerResponce = JSON.parse(xhr.responseText);

        var statusCode = parseInt(_JCWebClient_Static.SCLayerResponce.Status.Code);

        if (0 != statusCode) {
            debugLog(_JCWebClient_Static.SCLayerResponce.Status.Message);
            debugLog("[JCWebClient] " + RequestObj.jcapi + " failed: with status " + _JCWebClient_Static.SCLayerResponce.Status.Code);

            throw new Error(_JCWebClient_Static.SCLayerResponce.Status.Message);
        } else {
            debugLog("[JCWebClient] " + RequestObj.jcapi + " succeeded.");
        }

        var typedResponse = constructTypedResponse(_JCWebClient_Static.SCLayerResponce.ResultingData);
        _JCWebClient_Static.SCLayerResponce.ResultingData = typedResponse;

        return _JCWebClient_Static.SCLayerResponce.ResultingData;
    }

    function requestJcExtFunction2Async(RequestObj, callback) {
        if (_JCWebClient_Static.asyncRequestIsOngoing) {
            return;
        }
        _JCWebClient_Static.asyncRequestIsOngoing = true;
        _JCWebClient_Static.SCLayerResponce = null;

        RequestObj.session_id = _JCWebClient_Static.session_id;
        var jsonRequest = JSON.stringify(RequestObj);

        var xhr = new XMLHttpRequest;
        var url = _JCWebClient_Static.requestUrl;
        var multiGetRequest = false;

        function XhrReadyStateHandler() {
            if (this.readyState == 4) {
                if (typeof(this.status) != "unknown" && this.status == 200) {
                    try {
                        var prefix = xhr.responseText.substring(0, 5); // [GET]

                        if ('[GET]' == prefix) {
                            if (!multiGetRequest) {
                                json_parse.init();
                                multiGetRequest = true;
                            }

                            json_parse.parse(xhr.responseText.substring(5));

                            xhr.open('GET', _JCWebClient_Static.requestUrl + "session_id=" + _JCWebClient_Static.session_id, true);
                            xhr.send();
                        }
                        else {
                            _JCWebClient_Static.asyncRequestIsOngoing = false;
                            if (multiGetRequest) {
                                json_parse.parse(xhr.responseText);

                                if (json_parse.wellformed())
                                    _JCWebClient_Static.SCLayerResponce = json_parse.result();
                                else
                                    throw new SyntaxError('JSON');
                            }
                            else
                                _JCWebClient_Static.SCLayerResponce = JSON.parse(xhr.responseText);

                            var statusCode = parseInt(_JCWebClient_Static.SCLayerResponce.Status.Code);

                            if (0 != statusCode) {
                                debugLog(_JCWebClient_Static.SCLayerResponce.Status.Message);
                                debugLog("[JCWebClient] " + RequestObj.jcapi + " failed: with status " + _JCWebClient_Static.SCLayerResponce.Status.Code);

                                throw new Error(_JCWebClient_Static.SCLayerResponce.Status.Message);
                            } else {
                                debugLog("[JCWebClient] " + RequestObj.jcapi + " succeeded.");
                            }

                            var typedResponse = constructTypedResponse(_JCWebClient_Static.SCLayerResponce.ResultingData);
                            _JCWebClient_Static.SCLayerResponce.ResultingData = typedResponse;

                            callback(_JCWebClient_Static.SCLayerResponce.ResultingData);
                        }
                    }
                    catch (arg) {
                        _JCWebClient_Static.asyncRequestIsOngoing = false;
                        var error;
                        if (arg instanceof Error || arg instanceof SyntaxError) {
                            error = ["Error", arg.message];
                        }
                        else {
                            error = ["Error", "Unspecified error."];
                        }
                        callback(error);
                    }
                }
                else {
                    _JCWebClient_Static.asyncRequestIsOngoing = false;

                    var message;
                    if (typeof(this.status) != "unknown")
                        message = "[JCWebClient] POST for " + RequestObj.jcapi + " failed: with status " + xhr.status;
                    else
                        message = "[JCWebClient] POST for " + RequestObj.jcapi + " failed: with unknown status";
                    debugLog(message);

                    var error = ["Error", message];
                    callback(error);
                }
            }
        }

        xhr.onreadystatechange = XhrReadyStateHandler;
        xhr.open('POST', url, true);
        xhr.send(jsonRequest);
    }

    function debugLog(str) {
        if (typeof(console) != "undefined")
            console.log(str);
    }

    // Internal API functions
    function Ping() {
        var RequestObj = new Object();
        RequestObj.jcapi = "ping";
        RequestObj.ticket_id = "0";

        return requestJcExtFunction2(RequestObj);
    }

    function getAllTokens() {
        var RequestObj = new Object();
        RequestObj.jcapi = "getAllTokens";

        return requestJcExtFunction2(RequestObj);
    }

    function getAllSlots() {
        var RequestObj = new Object();
        RequestObj.jcapi = "getAllSlots";

        return requestJcExtFunction2(RequestObj);
    }

    function getTokenInfo(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getTokenInfo";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function getSlotInfo(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getTokenInfo";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function getLoggedInState() {
        var RequestObj = new Object();
        RequestObj.jcapi = "getLoggedInState";

        return requestJcExtFunction2(RequestObj);
    }

    function getPluginVersion() {
        var RequestObj = new Object();
        RequestObj.jcapi = "getPluginVersion";

        return requestJcExtFunction2(RequestObj);
    }

    function checkWebBrowserVersion() {
        var RequestObj = new Object();
        RequestObj.jcapi = "checkWebBrowserVersion";

        return requestJcExtFunction2(RequestObj);
    }

    function initToken(SlotId, AdminPin, UserPin, SCLabel) {
        var RequestObj = new Object();
        RequestObj.jcapi = "initToken";
        RequestObj.SlotId = SlotId;
        RequestObj.UserPin = UserPin;
        RequestObj.AdminPin = AdminPin;
        RequestObj.Label = SCLabel;

        return requestJcExtFunction2(RequestObj);
    }

    function initTokenWithoutUserPIN(SlotId, AdminPin, SCLabel) {
        var RequestObj = new Object();
        RequestObj.jcapi = "initToken";
        RequestObj.SlotId = SlotId;
        RequestObj.UserPin = ""; // pass an empty string
        RequestObj.AdminPin = AdminPin;
        RequestObj.Label = SCLabel;

        return requestJcExtFunction2(RequestObj);
    }

    function initTokenUI(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "initTokenUI";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function getSystemInfo(siclass) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getSystemInfo";
        RequestObj.Class = siclass;

        return requestJcExtFunction2(RequestObj);
    }

    function getSystemInfoAsync(callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getSystemInfo";

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    // Error handling
    function getLastError() {
        var RequestObj = new Object();
        RequestObj.jcapi = "getLastError";

        return requestJcExtFunction2(RequestObj);
    }

    function getErrorMessage(Error) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getErrorMessage";
        RequestObj.Error = Error;
        return requestJcExtFunction2(RequestObj);
    }

    function isAsyncOperationInProgress() {
        if (_JCWebClient_Static.asyncRequestIsOngoing) {
            return true;
        }
        else {
            return false;
        }
    }

    // PIN code operations
    function bindToken(SlotId, sPin, userType) {
        var RequestObj = new Object();
        RequestObj.jcapi = "bindToken";
        RequestObj.SlotId = SlotId;
        RequestObj.Pin = sPin;

        if (typeof(userType) != 'undefined') {
            RequestObj.Type = userType;
        }

        return requestJcExtFunction2(RequestObj);
    }

    function bindTokenAsync(SlotId, sPin, userTypeOrCallback, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "bindToken";
        RequestObj.SlotId = SlotId;
        RequestObj.Pin = sPin;

        if (typeof (userTypeOrCallback) == 'function') {
            callback = userTypeOrCallback;
        }
        else {
            RequestObj.Type = userTypeOrCallback;
        }

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function bindTokenUI(SlotId, userType) {
        var RequestObj = new Object();
        RequestObj.jcapi = "bindTokenUI";
        RequestObj.SlotId = SlotId;

        if (typeof(userType) != 'undefined') {
            RequestObj.Type = userType;
        }

        return requestJcExtFunction2(RequestObj);
    }

    function unbindToken() {
        var RequestObj = new Object();
        RequestObj.jcapi = "unbindToken";

        return requestJcExtFunction2(RequestObj);
    }

    function changePIN(SlotId, userType, oldPin, newPin) {
        var RequestObj = new Object();
        RequestObj.jcapi = "changePIN";
        RequestObj.SlotId = SlotId;
        RequestObj.Type = userType;
        RequestObj.Pin = oldPin;
        RequestObj.NewPin = newPin;

        return requestJcExtFunction2(RequestObj);
    }

    function changePINAsync(SlotId, userType, oldPin, newPin, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "changePIN";
        RequestObj.SlotId = SlotId;
        RequestObj.Type = userType;
        RequestObj.Pin = oldPin;
        RequestObj.NewPin = newPin;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function changePINUI(SlotId, userType) {
        var RequestObj = new Object();
        RequestObj.jcapi = "changePINUI";
        RequestObj.SlotId = SlotId;
        RequestObj.Type = userType;

        return requestJcExtFunction2(RequestObj);
    }

    function changePINUIAsync(SlotId, userType, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "changePINUI";
        RequestObj.SlotId = SlotId;
        RequestObj.Type = userType;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function initUserPIN(SlotId, AdminPin, userPin) {
        var RequestObj = new Object();
        RequestObj.jcapi = "initUserPIN";
        RequestObj.SlotId = SlotId;
        RequestObj.AdminPin = AdminPin;
        RequestObj.UserPin = userPin;

        return requestJcExtFunction2(RequestObj);
    }

    function initUserPINAsync(SlotId, AdminPin, userPin, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "initUserPIN";
        RequestObj.SlotId = SlotId;
        RequestObj.AdminPin = AdminPin;
        RequestObj.UserPin = userPin;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function initUserPINUI(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "initUserPINUI";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function initUserPINUIAsync(SlotId, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "initUserPINUI";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function initUserPINAFT(SlotId, LangID, RequireConfirmation) {
        var RequestObj = new Object();
        RequestObj.jcapi = "initUserPINAFT";
        RequestObj.SlotId = SlotId;
        RequestObj.LangID = LangID;
        RequestObj.RequireConfirmation = RequireConfirmation;

        return requestJcExtFunction2(RequestObj);
    }

    function unblockUserPIN(SlotId, AdminPin) {
        var RequestObj = new Object();
        RequestObj.jcapi = "unblockUserPIN";
        RequestObj.SlotId = SlotId;
        RequestObj.AdminPin = AdminPin;

        return requestJcExtFunction2(RequestObj);
    }

    function unblockUserPINUI(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "unblockUserPINUI";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function unblockUserPINasync(SlotId, AdminPin, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "unblockUserPIN";
        RequestObj.SlotId = SlotId;
        RequestObj.AdminPin = AdminPin;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function unblockUserPINUIAsync(SlotId, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "unblockUserPINUI";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    // Container operations
    function createContainer(ecParams, Description) {
        var RequestObj = new Object();
        RequestObj.jcapi = "createContainer";
        RequestObj.ecParams = ecParams;
        RequestObj.Description = Description;

        return requestJcExtFunction2(RequestObj);
    }

    function createContainerEx(CkaID, ecParams, Description) {
        var RequestObj = new Object();
        RequestObj.jcapi = "createContainerEx";
        RequestObj.CkaID = CkaID;
        RequestObj.ecParams = ecParams;
        RequestObj.Description = Description;

        return requestJcExtFunction2(RequestObj);
    }

    function createContainerAsync(ecParams, Description, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "createContainer";
        RequestObj.ecParams = ecParams;
        RequestObj.Description = Description;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function createContainerExAsync(CkaID, ecParams, Description, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "createContainerEx";
        RequestObj.CkaID = CkaID;
        RequestObj.ecParams = ecParams;
        RequestObj.Description = Description;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function deleteContainerOrCertificate(ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "deleteContainerOrCertificate";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function deleteCertificate(ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "deleteCertificate";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function changeContainerDescription(ContId, Description) {
        var RequestObj = new Object();
        RequestObj.jcapi = "changeContainerDescription";
        RequestObj.ContId = ContId;
        RequestObj.Description = Description;

        return requestJcExtFunction2(RequestObj);
    }

    function genCSR(ContId, Dn, Exts) {
        var RequestObj = new Object();
        RequestObj.jcapi = "genCSR";
        RequestObj.ContId = ContId;
        RequestObj.Dn = Dn;
        RequestObj.Exts = Exts;

        return requestJcExtFunction2(RequestObj);
    }

    function genCSRAsync(ContId, Dn, Exts, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "genCSR";
        RequestObj.ContId = ContId;
        RequestObj.Dn = Dn;
        RequestObj.Exts = Exts;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function genCSRUsingHardwareHash(ContId, Dn, Exts) {
        var RequestObj = new Object();
        RequestObj.jcapi = "genCSRUsingHardwareHash";
        RequestObj.ContId = ContId;
        RequestObj.Dn = Dn;
        RequestObj.Exts = Exts;

        return requestJcExtFunction2(RequestObj);
    }

    function genCSRUsingHardwareHashAsync(ContId, Dn, Exts, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "genCSRUsingHardwareHash";
        RequestObj.ContId = ContId;
        RequestObj.Dn = Dn;
        RequestObj.Exts = Exts;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function writeSelfSignedCertificate(ContId, Dn, Exts, Days) {
        var RequestObj = new Object();
        RequestObj.jcapi = "writeSelfSignedCertificate";
        RequestObj.ContId = ContId;
        RequestObj.Dn = Dn;
        RequestObj.Exts = Exts;
        RequestObj.Days = Days;

        return requestJcExtFunction2(RequestObj);
    }

    function writeCertificate(ContId, Cert) {
        var RequestObj = new Object();
        RequestObj.jcapi = "writeCertificate";
        RequestObj.ContId = ContId;
        RequestObj.Cert = Cert;

        return requestJcExtFunction2(RequestObj);
    }

    function writeCertificateAsync(ContId, Cert, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "writeCertificate";
        RequestObj.ContId = ContId;
        RequestObj.Cert = Cert;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function writeSignerCertificate(Cert, Description) {
        var RequestObj = new Object();
        RequestObj.jcapi = "writeSignerCertificate";
        RequestObj.Cert = Cert;
        RequestObj.Description = Description;

        return requestJcExtFunction2(RequestObj);
    }

    function writeSignerCertificateAsync(Cert, Description, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "writeSignerCertificate";
        RequestObj.Cert = Cert;
        RequestObj.Description = Description;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function deleteSignerCertificateById(ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "deleteSignerCertificateById";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function deleteAllSignerCertificatesForId(ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "deleteAllSignerCertificatesForId";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function deleteAllSignerCertificates() {
        var RequestObj = new Object();
        RequestObj.jcapi = "deleteAllSignerCertificates";

        return requestJcExtFunction2(RequestObj);
    }

    function readCertificate(ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "readCertificate";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function readCertificateEx(SlotId, ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "readCertificateEx";
        RequestObj.SlotId = SlotId;
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function readSignerCertificateList(ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "readSignerCertificateList";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function readSignerCertificateListAsync(ContId, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "readSignerCertificateList";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function writeServerPublicKey(ContId, Spk) {
        var RequestObj = new Object();
        RequestObj.jcapi = "writeServerPublicKey";
        RequestObj.ContId = ContId;
        RequestObj.Spk = Spk;

        return requestJcExtFunction2(RequestObj);
    }

    function readServerPublicKey(ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "readServerPublicKey";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function writeServerCertificate(ContId, Cert) {
        var RequestObj = new Object();
        RequestObj.jcapi = "writeServerCertificate";
        RequestObj.ContId = ContId;
        RequestObj.Cert = Cert;

        return requestJcExtFunction2(RequestObj);
    }

    function readServerCertificate(ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "readServerCertificate";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function readCkaID(ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "readCkaID";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function writeCkaID(ContId, newCkaID) {
        var RequestObj = new Object();
        RequestObj.jcapi = "writeCkaID";
        RequestObj.ContId = ContId;
        RequestObj.CkaID = newCkaID;

        return requestJcExtFunction2(RequestObj);
    }

    function readPublicKey(SlotId, ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "readPublicKey";
        RequestObj.SlotId = SlotId;
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function getCertificateInfo(SlotId, ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getCertificateInfo";
        RequestObj.SlotId = SlotId;
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function getCertificatePublicKey(Cert) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getCertificatePublicKey";
        RequestObj.Cert = Cert;

        return requestJcExtFunction2(RequestObj);
    }

    function getContainerList(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getContainerList";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function parseCertificateEx(SlotId, ContId) {
        var res = getCertificateInfo(SlotId, ContId);

        var str = "";
        for (var i = 0; i < res.length; i++) {
            str += String.fromCharCode(res[i]);
        }
        str = decodeURIComponent(escape(str));

        return parseCertInfoString(str);
    }

    function parseCertificate(Cert) {
        var res = getCertificateInfoEx(Cert);

        var str = "";
        for (var i = 0; i < res.length; i++) {
            str += String.fromCharCode(res[i]);
        }
        str = decodeURIComponent(escape(str));

        return parseCertInfoString(str);
    }

    function parseX509CertificateEx(SlotId, ContId) {
        var res = getCertificateInfo(SlotId, ContId);

        var str = "";
        for (var i = 0; i < res.length; i++) {
            str += String.fromCharCode(res[i]);
        }
        str = decodeURIComponent(escape(str));

        return parseCertInfoString(str, true);
    }

    function parseX509Certificate(Cert) {
        var res = getCertificateInfoEx(Cert);

        var str = "";
        for (var i = 0; i < res.length; i++) {
            str += String.fromCharCode(res[i]);
        }
        str = decodeURIComponent(escape(str));

        return parseCertInfoString(str, true);
    }

    function getCertificateList(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getCertificateList";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function getCertificateListAsync(SlotId, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getCertificateList";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function getCertificateListEx(SlotId, Sn, Issuer, Subject) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getCertificateListEx";
        RequestObj.SlotId = SlotId;
        RequestObj.Sn = Sn;
        RequestObj.Issuer = Issuer;
        RequestObj.Subject = Subject;

        return requestJcExtFunction2(RequestObj);
    }

    function getCertificateListExAsync(SlotId, Sn, Issuer, Subject, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getCertificateListEx";
        RequestObj.SlotId = SlotId;
        RequestObj.Sn = Sn;
        RequestObj.Issuer = Issuer;
        RequestObj.Subject = Subject;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function getSignerCertificateList(ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getSignerCertificateList";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    function getSignerCertificateListAsync(ContId, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getSignerCertificateList";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function getAllValidCertificateChains() {
        var RequestObj = new Object();
        RequestObj.jcapi = "getAllValidCertificateChains";

        return requestJcExtFunction2(RequestObj);
    }

    function getAllValidCertificateChainsAsync(callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getAllValidCertificateChains";

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function getAllInvalidCertificateChains() {
        var RequestObj = new Object();
        RequestObj.jcapi = "getAllInvalidCertificateChains";

        return requestJcExtFunction2(RequestObj);
    }

    function getAllInvalidCertificateChainsAsync(callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getAllInvalidCertificateChains";

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function getAllUnusedCertificateChains() {
        var RequestObj = new Object();
        RequestObj.jcapi = "getAllUnusedCertificateChains";

        return requestJcExtFunction2(RequestObj);
    }

    function getAllUnusedCertificateChainsAsync(callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getAllUnusedCertificateChains";

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function getAllCertificateChains() {
        var RequestObj = new Object();
        RequestObj.jcapi = "getAllCertificateChains";

        return requestJcExtFunction2(RequestObj);
    }

    function getAllCertificateChainsAsync(callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getAllCertificateChains";

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function createStandaloneCertificate(Cert, Description) {
        var RequestObj = new Object();
        RequestObj.jcapi = "createStandaloneCertificate";
        RequestObj.Cert = Cert;
        RequestObj.Description = Description;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyCertificateChain(Cert, TrustedCerts, CertChain) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyCertificateChain";
        RequestObj.Cert = Cert;
        RequestObj.TrustedCerts = TrustedCerts;
        RequestObj.CertChain = CertChain;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyCertificateChainAsync(Cert, TrustedCerts, CertChain, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyCertificateChain";
        RequestObj.Cert = Cert;
        RequestObj.TrustedCerts = TrustedCerts;
        RequestObj.CertChain = CertChain;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function verifyCertificateChainEx(Cert, TrustedCerts, CertChain, RevokedCerts) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyCertificateChainEx";
        RequestObj.Cert = Cert;
        RequestObj.TrustedCerts = TrustedCerts;
        RequestObj.CertChain = CertChain;
        RequestObj.RevokedCerts = RevokedCerts;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyCertificateChainExAsync(Cert, TrustedCerts, CertChain, RevokedCerts, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyCertificateChainEx";
        RequestObj.Cert = Cert;
        RequestObj.TrustedCerts = TrustedCerts;
        RequestObj.CertChain = CertChain;
        RequestObj.RevokedCerts = RevokedCerts;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function verifyCertificateChainExExternalTrustedCerts(Cert, TrustedCerts, CertChain, RevokedCerts) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyCertificateChainExExternalTrustedCerts";
        RequestObj.Cert = Cert;
        RequestObj.TrustedCerts = TrustedCerts;
        RequestObj.CertChain = CertChain;
        RequestObj.RevokedCerts = RevokedCerts;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyCertificateChainExExternalTrustedCertsAsync(Cert, TrustedCerts, CertChain, RevokedCerts, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyCertificateChainExExternalTrustedCerts";
        RequestObj.Cert = Cert;
        RequestObj.TrustedCerts = TrustedCerts;
        RequestObj.CertChain = CertChain;
        RequestObj.RevokedCerts = RevokedCerts;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    // Signing and signature verification
    function enableReverifyPINOnSignature(ReverifyPin) {
        var RequestObj = new Object();
        RequestObj.jcapi = "enableReverifyPINOnSignature";
        RequestObj.ReverifyPin = ReverifyPin;

        return requestJcExtFunction2(RequestObj);
    }

    function isReverifyPINOnSignatureEnabled() {
        var RequestObj = new Object();
        RequestObj.jcapi = "isReverifyPINOnSignatureEnabled";

        return requestJcExtFunction2(RequestObj);
    }

    function signData(ContId, Data, AttachedSignature) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signData";
        RequestObj.ContId = ContId;
        RequestObj.Data = Data;
        RequestObj.AttachedSignature = AttachedSignature;

        return requestJcExtFunction2(RequestObj);
    }

    function signDataAsync(ContId, Data, AttachedSignature, UseHardwareHash, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signData";
        RequestObj.ContId = ContId;
        RequestObj.Data = Data;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.UseHardwareHash = UseHardwareHash;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function signBase64EncodedData(ContId, DataBase64, AttachedSignature) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signDataBase64";
        RequestObj.ContId = ContId;
        RequestObj.DataBase64 = DataBase64;
        RequestObj.AttachedSignature = AttachedSignature;

        return requestJcExtFunction2(RequestObj);
    }

    function signBase64EncodedDataAsync(ContId, DataBase64, AttachedSignature, UseHardwareHash, callback) {
        var RequestObj = new Object();
        if (UseHardwareHash == true)
            RequestObj.jcapi = "signDataUsingHardwareHashBase64";
        else
            RequestObj.jcapi = "signDataBase64";
        RequestObj.ContId = ContId;
        RequestObj.DataBase64 = DataBase64;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.UseHardwareHash = UseHardwareHash;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function signDataInFile(ContId, FileName, AttachedSignature) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signDataInFile";
        RequestObj.ContId = ContId;
        RequestObj.FileName = FileName;
        RequestObj.AttachedSignature = AttachedSignature;

        return requestJcExtFunction2(RequestObj);
    }

    function signDataInFileBase64(ContId, FileName, AttachedSignature) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signDataInFileBase64";
        RequestObj.ContId = ContId;
        RequestObj.FileName = FileName;
        RequestObj.AttachedSignature = AttachedSignature;

        return requestJcExtFunction2(RequestObj);
    }

    function signDataUsingHardwareHashBase64(ContId, DataBase64, AttachedSignature) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signDataUsingHardwareHashBase64";
        RequestObj.ContId = ContId;
        RequestObj.DataBase64 = DataBase64;
        RequestObj.AttachedSignature = AttachedSignature;

        return requestJcExtFunction2(RequestObj);
    }

    function signDataInFileUsingHardwareHash(ContId, FileName, AttachedSignature) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signDataInFileUsingHardwareHash";
        RequestObj.ContId = ContId;
        RequestObj.FileName = FileName;
        RequestObj.AttachedSignature = AttachedSignature;

        return requestJcExtFunction2(RequestObj);
    }

    function signDataInFileUsingHardwareHashBase64(ContId, FileName, AttachedSignature) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signDataInFileUsingHardwareHashBase64";
        RequestObj.ContId = ContId;
        RequestObj.FileName = FileName;
        RequestObj.AttachedSignature = AttachedSignature;

        return requestJcExtFunction2(RequestObj);
    }

    function signDataUsingHardwareHash(ContId, Data, AttachedSignature) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signDataUsingHardwareHash";
        RequestObj.ContId = ContId;
        RequestObj.Data = Data;
        RequestObj.AttachedSignature = AttachedSignature;

        return requestJcExtFunction2(RequestObj);
    }

    function signBase64EncodedDataUsingHardwareHash(ContId, DataBase64, AttachedSignature) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signDataUsingHardwareHashBase64";
        RequestObj.ContId = ContId;
        RequestObj.DataBase64 = DataBase64;
        RequestObj.AttachedSignature = AttachedSignature;

        return requestJcExtFunction2(RequestObj);
    }

    function signHash(ContId, Hash, AttachedSignature) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signHash";
        RequestObj.ContId = ContId;
        RequestObj.Hash = Hash;
        RequestObj.AttachedSignature = AttachedSignature;

        return requestJcExtFunction2(RequestObj);
    }

    function signDataInByte(ContId, Data, UseHardwareHash) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signDataInByte";
        RequestObj.ContId = ContId;
        RequestObj.Data = Data;
        RequestObj.UseHardwareHash = UseHardwareHash;

        return requestJcExtFunction2(RequestObj);
    }

    function signDataInByteAsync(ContId, Data, UseHardwareHash, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signDataInByte";
        RequestObj.ContId = ContId;
        RequestObj.Data = Data;
        RequestObj.UseHardwareHash = UseHardwareHash;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function signHashInByte(ContId, Hash) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signHashInByte";
        RequestObj.ContId = ContId;
        RequestObj.Hash = Hash;

        return requestJcExtFunction2(RequestObj);
    }

    function signHashInByteAsync(ContId, Hash, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "signHashInByte";
        RequestObj.ContId = ContId;
        RequestObj.Hash = Hash;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function verifyData(Signature, AttachedSignature, Data) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyData";
        RequestObj.Signature = Signature;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.Data = Data;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyDataHW(Signature, AttachedSignature, Data) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyDataHW";
        RequestObj.Signature = Signature;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.Data = Data;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyDataHWUsingHardwareHash(Signature, AttachedSignature, Data) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyDataHWUsingHardwareHash";
        RequestObj.Signature = Signature;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.Data = Data;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyBase64EncodedData(SignatureBase64, AttachedSignature, DataBase64) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyBase64EncodedData";
        RequestObj.SignatureBase64 = SignatureBase64;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.DataBase64 = DataBase64;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyBase64EncodedDataHW(SignatureBase64, AttachedSignature, DataBase64) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyBase64EncodedDataHW";
        RequestObj.SignatureBase64 = SignatureBase64;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.DataBase64 = DataBase64;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyBase64EncodedDataHWUsingHardwareHash(SignatureBase64, AttachedSignature, DataBase64) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyBase64EncodedDataHWUsingHardwareHash";
        RequestObj.SignatureBase64 = SignatureBase64;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.DataBase64 = DataBase64;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyRawBase64EncodedData(ContId, SignatureBase64, DataBase64) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyRawBase64EncodedData";
        RequestObj.ContId = ContId;
        RequestObj.SignatureBase64 = SignatureBase64;
        RequestObj.DataBase64 = DataBase64;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyRawBase64EncodedDataEx(SlotId, ContId, SignatureBase64, DataBase64) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyRawBase64EncodedDataEx";
        RequestObj.SlotId = SlotId;
        RequestObj.ContId = ContId;
        RequestObj.SignatureBase64 = SignatureBase64;
        RequestObj.DataBase64 = DataBase64;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyDataInFile(Signature, AttachedSignature, FileName) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyDataInFile";
        RequestObj.Signature = Signature;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.FileName = FileName;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyDataInByte(Signature, Data, PublicKeyValue) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyDataInByte";
        RequestObj.Signature = Signature;
        RequestObj.Data = Data;
        RequestObj.PublicKeyValue = PublicKeyValue;

        return requestJcExtFunction2(RequestObj);
    }

    function verifyHashInByte(Signature, Hash, PublicKeyValue) {
        var RequestObj = new Object();
        RequestObj.jcapi = "verifyHashInByte";
        RequestObj.Signature = Signature;
        RequestObj.Hash = Hash;
        RequestObj.PublicKeyValue = PublicKeyValue;

        return requestJcExtFunction2(RequestObj);
    }

    function digest(Data) {
        var RequestObj = new Object();
        RequestObj.jcapi = "digest";
        RequestObj.Data = Data;

        return requestJcExtFunction2(RequestObj);
    }

    function digestAsync(Data, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "digest";
        RequestObj.Data = Data;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function digestHardware(Data) {
        var RequestObj = new Object();
        RequestObj.jcapi = "digestHardware";
        RequestObj.Data = Data;

        return requestJcExtFunction2(RequestObj);
    }

    function digestHardwareAsync(Data, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "digestHardware";
        RequestObj.Data = Data;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function digestNoAuth(SlotId, Data) {
        var RequestObj = new Object();
        RequestObj.jcapi = "digestNoAuth";
        RequestObj.SlotId = SlotId;
        RequestObj.Data = Data;

        return requestJcExtFunction2(RequestObj);
    }

    function digestNoAuthAsync(SlotId, Data, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "digestNoAuth";
        RequestObj.SlotId = SlotId;
        RequestObj.Data = Data;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function digestNoAuthHardware(SlotId, Data) {
        var RequestObj = new Object();
        RequestObj.jcapi = "digestNoAuthHW";
        RequestObj.SlotId = SlotId;
        RequestObj.Data = Data;

        return requestJcExtFunction2(RequestObj);
    }

    function digestNoAuthHardwareAsync(SlotId, Data, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "digestNoAuthHW";
        RequestObj.SlotId = SlotId;
        RequestObj.Data = Data;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function pkcs7Parse(Data) {
        var RequestObj = new Object();
        RequestObj.jcapi = "pkcs7Parse";
        RequestObj.Data = Data;

        return requestJcExtFunction2(RequestObj);
    }

    function pkcs7ParseBase64Encoded(DataBase64) {
        var RequestObj = new Object();
        RequestObj.jcapi = "pkcs7ParseBase64";
        RequestObj.DataBase64 = DataBase64;

        return requestJcExtFunction2(RequestObj);
    }

    function pkcs7ParseInFile(FileName) {
        var RequestObj = new Object();
        RequestObj.jcapi = "pkcs7ParseInFile";
        RequestObj.FileName = FileName;

        return requestJcExtFunction2(RequestObj);
    }

    function pkcs7ParseInFileBase64Encoded(FileName) {
        var RequestObj = new Object();
        RequestObj.jcapi = "pkcs7ParseInFileBase64";
        RequestObj.FileName = FileName;

        return requestJcExtFunction2(RequestObj);
    }

    function makeContainerPkcs21Ready(ContId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "makeContainerPkcs21Ready";
        RequestObj.ContId = ContId;

        return requestJcExtFunction2(RequestObj);
    }

    // Authentication and channel protection
    function establishSChannelBegin(CertHandle) {
        var RequestObj = new Object();
        RequestObj.jcapi = "establishSChannelBegin";
        RequestObj.CertHandle = CertHandle;

        return requestJcExtFunction2(RequestObj);
    }

    function establishSChannelContinue(ServerTlsPacket, ConectionId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "establishSChannelContinue";
        RequestObj.ServerTlsPacket = ServerTlsPacket;
        RequestObj.ConectionId = ConectionId;

        return requestJcExtFunction2(RequestObj);
    }

    function unilateralAuthenticationBegin(CertHandle) {
        var RequestObj = new Object();
        RequestObj.jcapi = "unilateralAuthenticationBegin";
        RequestObj.CertHandle = CertHandle;

        return requestJcExtFunction2(RequestObj);
    }

    function unilateralAuthenticationContinue(ServerTlsPacket, ConectionId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "unilateralAuthenticationContinue";
        RequestObj.ServerTlsPacket = ServerTlsPacket;
        RequestObj.ConectionId = ConectionId;

        return requestJcExtFunction2(RequestObj);
    }

    function encode(PlainText) {
        var RequestObj = new Object();
        RequestObj.jcapi = "encode";
        RequestObj.PlainText = PlainText;

        return requestJcExtFunction2(RequestObj);
    }

    function decode(CipherText) {
        var RequestObj = new Object();
        RequestObj.jcapi = "decode";
        RequestObj.CipherText = CipherText;

        return requestJcExtFunction2(RequestObj);
    }

    // Data storage
    function storeBinaryData(Label, Data) {
        var RequestObj = new Object();
        RequestObj.jcapi = "storeBinaryData";
        RequestObj.Label = Label;
        RequestObj.Data = Data;

        return requestJcExtFunction2(RequestObj);
    }

    function storePrivateBinaryData(Label, Data) {
        var RequestObj = new Object();
        RequestObj.jcapi = "storePrivateBinaryData";
        RequestObj.Label = Label;
        RequestObj.Data = Data;

        return requestJcExtFunction2(RequestObj);
    }

    function getBinaryDataObjectList(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getBinaryDataObjectList";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function readBinaryObject(SlotId, ObjectId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "readBinaryObject";
        RequestObj.SlotId = SlotId;
        RequestObj.ObjectId = ObjectId;

        return requestJcExtFunction2(RequestObj);
    }

    function deleteBinaryObject(ObjectId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "deleteBinaryObject";
        RequestObj.ObjectId = ObjectId;

        return requestJcExtFunction2(RequestObj);
    }

    function modifyBinaryObject(ObjectId, Label, Data) {
        var RequestObj = new Object();
        RequestObj.jcapi = "modifyBinaryObject";
        RequestObj.ObjectId = ObjectId;
        RequestObj.Label = Label;
        RequestObj.Data = Data;

        return requestJcExtFunction2(RequestObj);
    }

    // Antifraud terminal and SWYX technology methods
    function isSwyxReader(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "isSwyxReader";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function enroll(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "enroll";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function performPersonalization(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "performPersonalization";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function performPersonalizationAsync(SlotId, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "performPersonalization";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function getReaderAppletSerialNumber(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getReaderAppletSerialNumber";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function swyxStart(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxStart";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function swyxStartEx(SlotId, Reference) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxStartEx";
        RequestObj.SlotId = SlotId;
        RequestObj.Reference = Reference;

        return requestJcExtFunction2(RequestObj);
    }

    function swyxStartAsync(SlotId, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxStart";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function swyxStartExAsync(SlotId, Reference, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxStartEx";
        RequestObj.SlotId = SlotId;
        RequestObj.Reference = Reference;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function swyxStop(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxStop";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function swyxStopAsync(SlotId, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxStop";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function swyxDisplay(SlotId, Message, Timeout) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxDisplay";
        RequestObj.SlotId = SlotId;
        RequestObj.Message = Message;
        RequestObj.Timeout = Timeout;

        return requestJcExtFunction2(RequestObj);
    }

    function swyxDisplayEx(SlotId, Message, Timeout, langId, MessageIdx) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxDisplayEx";
        RequestObj.SlotId = SlotId;
        RequestObj.Message = Message;
        RequestObj.Timeout = Timeout;
        RequestObj.LangID = langId;
        RequestObj.MessageIdx = MessageIdx;

        return requestJcExtFunction2(RequestObj);
    }

    function swyxDisplayAsync(SlotId, Message, Timeout, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxDisplay";
        RequestObj.SlotId = SlotId;
        RequestObj.Message = Message;
        RequestObj.Timeout = Timeout;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function swyxDisplayExAsync(SlotId, Message, Timeout, langId, MessageIdx, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxDisplayEx";
        RequestObj.SlotId = SlotId;
        RequestObj.Message = Message;
        RequestObj.Timeout = Timeout;
        RequestObj.LangID = langId;
        RequestObj.MessageIdx = MessageIdx;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function swyxSign(SlotId, ContId, Data, DisplayedMessage, AskPin, AttachedSignature, HardwareHash, Timeout) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxSign";
        RequestObj.SlotId = SlotId;
        RequestObj.ContId = ContId;
        RequestObj.Data = Data;
        RequestObj.DisplayedMessage = DisplayedMessage;
        RequestObj.AskPin = AskPin;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.HardwareHash = HardwareHash;
        RequestObj.Timeout = Timeout;

        return requestJcExtFunction2(RequestObj);
    }

    function swyxSignAsync(SlotId, ContId, Data, DisplayedMessage, AskPin, AttachedSignature, HardwareHash, Timeout, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxSign";
        RequestObj.SlotId = SlotId;
        RequestObj.ContId = ContId;
        RequestObj.Data = Data;
        RequestObj.DisplayedMessage = DisplayedMessage;
        RequestObj.AskPin = AskPin;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.HardwareHash = HardwareHash;
        RequestObj.Timeout = Timeout;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function swyxSignEx(SlotId, ContId, Data, DisplayedMessage, AskPin, AttachedSignature, HardwareHash, Timeout, Reference, LangID, MessageIdx) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxSignEx";
        RequestObj.SlotId = SlotId;
        RequestObj.ContId = ContId;
        RequestObj.Data = Data;
        RequestObj.DisplayedMessage = DisplayedMessage;
        RequestObj.AskPin = AskPin;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.HardwareHash = HardwareHash;
        RequestObj.Timeout = Timeout;
        RequestObj.Reference = Reference;
        RequestObj.LangID = LangID;
        RequestObj.MessageIdx = MessageIdx;

        return requestJcExtFunction2(RequestObj);
    }

    function swyxSignExAsync(SlotId, ContId, Data, DisplayedMessage, AskPin, AttachedSignature, HardwareHash, Timeout, Reference, LangID, MessageIdx, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "swyxSignEx";
        RequestObj.SlotId = SlotId;
        RequestObj.ContId = ContId;
        RequestObj.Data = Data;
        RequestObj.DisplayedMessage = DisplayedMessage;
        RequestObj.AskPin = AskPin;
        RequestObj.AttachedSignature = AttachedSignature;
        RequestObj.HardwareHash = HardwareHash;
        RequestObj.Timeout = Timeout;
        RequestObj.Reference = Reference;
        RequestObj.LangID = LangID;
        RequestObj.MessageIdx = MessageIdx;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function aftEnterAdminPINAsync(SlotId, LangID, Timeout, ConfirmationCode, Message1Idx, Message2Idx, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "aftEnterAdminPIN";
        RequestObj.SlotId = SlotId;
        RequestObj.LangID = LangID;
        RequestObj.Timeout = Timeout;
        RequestObj.ConfirmationCode = ConfirmationCode;
        RequestObj.MessageIdx = Message1Idx;
        RequestObj.Message2Idx = Message2Idx;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function aftSaveAdminPINAsync(SlotId, LangID, Timeout, AdminPin, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "aftSaveAdminPIN";
        RequestObj.SlotId = SlotId;
        RequestObj.LangID = LangID;
        RequestObj.Timeout = Timeout;
        RequestObj.AdminPin = AdminPin;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function aftInitCardAsync(SlotId, LangID, Timeout, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "aftInitCard";
        RequestObj.SlotId = SlotId;
        RequestObj.LangID = LangID;
        RequestObj.Timeout = Timeout;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function aftInitUserPINAsync(SlotId, LangID, Timeout, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "aftInitUserPIN";
        RequestObj.SlotId = SlotId;
        RequestObj.LangID = LangID;
        RequestObj.Timeout = Timeout;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function bindTokenAFTAsync(SlotId, LangID, Timeout, MessageIdx, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "bindTokenAFT";
        RequestObj.SlotId = SlotId;
        RequestObj.LangID = LangID;
        RequestObj.Timeout = Timeout;
        RequestObj.MessageIdx = MessageIdx;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function aftGetReaderVersion(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "aftGetReaderVersion";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function aftCardlessSupport(SlotId) {
        var RequestObj = new Object();
        RequestObj.jcapi = "aftCardlessSupport";
        RequestObj.SlotId = SlotId;

        return requestJcExtFunction2(RequestObj);
    }

    function aftGetPINAsync(SlotId, LangID, Timeout, MessageIdx, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "aftGetPIN";
        RequestObj.SlotId = SlotId;
        RequestObj.LangID = LangID;
        RequestObj.Timeout = Timeout;
        RequestObj.MessageIdx = MessageIdx;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function aftGetNewPINAsync(SlotId, LangID, Timeout, Message1Idx, Message2Idx, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "aftGetNewPIN";
        RequestObj.SlotId = SlotId;
        RequestObj.LangID = LangID;
        RequestObj.Timeout = Timeout;
        RequestObj.MessageIdx = Message1Idx;
        RequestObj.Message2Idx = Message2Idx;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function aftUpdateFirmwareAsync(SlotId, Data, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "aftUpdateFirmware";
        RequestObj.SlotId = SlotId;
        RequestObj.Data = Data;

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function aftGetBrokenReaders() {
        var RequestObj = new Object();
        RequestObj.jcapi = "aftGetBrokenReaders";

        return requestJcExtFunction2(RequestObj);
    }

    function aftFixBrokenReaderAsync(ReaderName, Data, callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "aftFixBrokenReader";
        RequestObj.ReaderName = ReaderName;
        RequestObj.Data = Data;

        return requestJcExtFunction2Async(RequestObj, callback);
    }


    /*!
     * \fn RetrieveEvents()
     * \memberof JCWebClient
     *
     */
    function RetrieveEvents() {

        if (_JCWebClient_Static.eventRetriveCheckTimeoutId != null)
            clearTimeout(_JCWebClient_Static.eventRetriveCheckTimeoutId);

        var RequestObj = new Object();
        RequestObj.jcapi = "RetrieveEvents";
        RequestObj.session_id = _JCWebClient_Static.session_id;
        var jsonRequest = JSON.stringify(RequestObj);

        var xhr = new XMLHttpRequest;
        var url = _JCWebClient_Static.requestUrl;

        xhr.open('POST', url, true);

        xhr.onreadystatechange = RetrieveEventsStateHandler;
        xhr.timeout = 10000;

        xhr.send(jsonRequest);

        _JCWebClient_Static.eventRetriveCheckTimeoutId = setTimeout(RetrieveEventsCheckTimeoutHandler, 6000);

        function RetrieveEventsCheckTimeoutHandler() {
            xhr.abort();
        }

        function RetrieveEventsStateHandler() {
            if (this.readyState == 4) { // DONE
                if (_JCWebClient_Static.eventRetriveCheckTimeoutId != null) {
                    clearTimeout(_JCWebClient_Static.eventRetriveCheckTimeoutId);
                    _JCWebClient_Static.eventRetriveCheckTimeoutId = null;
                }

                if (typeof (this.status) != "unknown" && this.status == 200) {
                    try {

                        var NotificationResponse;

                        var NotificationResponse = JSON.parse(xhr.responseText);

                        var statusCode = parseInt(NotificationResponse.Status.Code);

                        if (0 == statusCode) {

                            var i;
                            try {
                                for (i = 0; i < NotificationResponse.ResultingData.length; i++) {
                                    var Event = NotificationResponse.ResultingData[i];
                                    var EventMethod = Event[0];
                                    var EventInfo = parseInt(Event[1]);

                                    debugLog("[JCWebClient::RetrieveEventsStateHandler] " + EventMethod + " : " + EventInfo);


                                    var j;
                                    if (EventMethod == "eTokenAdded") {
                                        for (j = 0; j < _JCWebClient_Static.TokenAddedSubscriptions.length; j++) {
                                            (_JCWebClient_Static.TokenAddedSubscriptions[j])(EventInfo);
                                        }
                                    } else if (EventMethod == "eTokenRemoved") {
                                        for (j = 0; j < _JCWebClient_Static.TokenRemovedSubscriptions.length; j++) {
                                            (_JCWebClient_Static.TokenRemovedSubscriptions[j])(EventInfo);
                                        }
                                    } else if (EventMethod == "LoginStateChanged") {
                                        for (j = 0; j < _JCWebClient_Static.LoginStateChangedSubscriptions.length; j++) {
                                            (_JCWebClient_Static.LoginStateChangedSubscriptions[j])(EventInfo);
                                        }
                                    } else if (EventMethod == "eSmartCardAdded") {
                                        for (j = 0; j < _JCWebClient_Static.SmartCardAddedSubscriptions.length; j++) {
                                            (_JCWebClient_Static.SmartCardAddedSubscriptions[j])(EventInfo);
                                        }
                                    } else if (EventMethod == "eSmartCardRemoved") {
                                        for (j = 0; j < _JCWebClient_Static.SmartCardRemovedSubscriptions.length; j++) {
                                            (_JCWebClient_Static.SmartCardRemovedSubscriptions[j])(EventInfo);
                                        }
                                    }
                                }
                            }
                            catch (errMessage) {
                                debugLog("[JCWebClient::RetrieveEventsStateHandler] exception caught:");
                                debugLog("[JCWebClient::RetrieveEventsStateHandler] " + errMessage);
                            }
                        } else {
                            debugLog(NotificationResponse.Status.Message);
                            debugLog("[JCWebClient::RetrieveEventsStateHandler] " + RequestObj.jcapi + " failed: with status " + NotificationResponse.Status.Code);
                        }
                    }
                    catch (arg) {
                    }

                }

                RetrieveEventsTimeoutHandler();
            }
        }
    }

    function RetrieveEventsTimeoutHandler() {
        setTimeout(RetrieveEvents, 1000);
    }

    /*!
     * \fn addEventListener(name, func, bubbling)
     * \memberof JCWebClient
     */
    function addEventListener(name, func, bubbling) {
        var strTokenAdded = "tokenAdded";
        var strTokenRemoved = "tokenRemoved";
        var strLoginStateChanged = "loginStateChanged";
        var strSmartCardAdded = "smartcardadded";
        var strSmartCardRemoved = "smartcardremoved";

        if (name.toUpperCase() == strTokenAdded.toUpperCase()) {
            _JCWebClient_Static.TokenAddedSubscriptions.push(func);
        } else if (name.toUpperCase() == strTokenRemoved.toUpperCase()) {
            _JCWebClient_Static.TokenRemovedSubscriptions.push(func);
        } else if (name.toUpperCase() == strLoginStateChanged.toUpperCase()) {
            _JCWebClient_Static.LoginStateChangedSubscriptions.push(func);
        } else if (name.toUpperCase() == strSmartCardAdded.toUpperCase()) {
            _JCWebClient_Static.SmartCardAddedSubscriptions.push(func);
        } else if (name.toUpperCase() == strSmartCardRemoved.toUpperCase()) {
            _JCWebClient_Static.SmartCardRemovedSubscriptions.push(func);
        }
    }

    /*!
     * \fn attachEvent(onName, func)
     * \memberof JCWebClient
     */
    function attachEvent(onName, func) {
        addEventListener(onName.substring(2), func, false); // ontokenAddedEvent-> tokenAddedEvent,...
    }

    /*!
     * \fn removeEventListener(name, func)
     * \memberof JCWebClient
     */
    function removeEventListener(name, func) {
        var strTokenAdded = "tokenAdded";
        var strTokenRemoved = "tokenRemoved";
        var strLoginStateChanged = "loginStateChanged";
        var strSmartCardAdded = "smartcardadded";
        var strSmartCardRemoved = "smartcardremoved";
        var funcArray;

        if (name.toUpperCase() == strTokenAdded.toUpperCase()) {
            funcArray = _JCWebClient_Static.TokenAddedSubscriptions;
        } else if (name.toUpperCase() == strTokenRemoved.toUpperCase()) {
            funcArray = _JCWebClient_Static.TokenRemovedSubscriptions;
        } else if (name.toUpperCase() == strLoginStateChanged.toUpperCase()) {
            funcArray = _JCWebClient_Static.LoginStateChangedSubscriptions;
        } else if (name.toUpperCase() == strSmartCardAdded.toUpperCase()) {
            funcArray = _JCWebClient_Static.SmartCardAddedSubscriptions;
        } else if (name.toUpperCase() == strSmartCardRemoved.toUpperCase()) {
            funcArray = _JCWebClient_Static.SmartCardRemovedSubscriptions;
        }
        try {
            var i;
            for (i = 0; i < funcArray.length; i++) {
                if (func == funcArray[i]) {
                    funcArray.splice(i, 1);
                    break;
                }
            }
        }
        catch (errMessage) {
            debugLog("[JCWebClient::removeEventListener] exception caught:");
            debugLog("[JCWebClient::removeEventListener] " + errMessage);
        }
    }

    /*!
     * \fn detachEvent(onName, func)
     * \memberof JCWebClient
     */
    function detachEvent(onName, func) {
        removeEventListener(onName.substring(2), func); // ontokenAddedEvent-> tokenAddedEvent,...
    }

    function closeWebSession() {
        var RequestObj = new Object();
        RequestObj.jcapi = "closeWebSession";

        return requestJcExtFunction2(RequestObj);
    }

    // Debug
    function debugFunction() {
        var RequestObj = new Object();
        RequestObj.jcapi = "debugFunction";

        return requestJcExtFunction2(RequestObj);
    }

    function debugFunctionAsynch(callback) {
        var RequestObj = new Object();
        RequestObj.jcapi = "debugFunction";

        return requestJcExtFunction2Async(RequestObj, callback);
    }

    function getCertificateInfoEx(Cert) {
        var RequestObj = new Object();
        RequestObj.jcapi = "getCertificateInfoEx";
        RequestObj.Cert = Cert;

        return requestJcExtFunction2(RequestObj);
    }

    function parseCertInfoString(info, objectizeRdn) {

        function structurizeCertInfo(info) {
            var lines = info.split('\n');

            var root = {childs: []};
            var que = [root];

            var level = -1;
            var extensionsLevel = -1;

            for (var i = 0; i < lines.length; i++) {
                var ln = lines[i];

                var spaceCount = 0;
                for (var ic = 0; ic < ln.length; ic++) {
                    if (ln[ic] != ' ') {
                        break;
                    }
                    spaceCount++;
                }

                if (spaceCount < 4 && extensionsLevel != -1 && level > extensionsLevel && ln.trim()) {
                    que[0].name += ('\n' + ln);
                    continue;
                }

                ln = ln.trim();
                if (!ln) {
                    continue;
                }

                if (spaceCount % 4) {
                    spaceCount += (4 - (spaceCount % 4));
                }
                var lnLevel = spaceCount / 4;

                var obj = {name: ln, childs: []};

                if (obj.name) {
                    var pos = obj.name.indexOf(': ');
                    if (pos != -1) {
                        obj.value = obj.name.substr(pos + 2).trim();
                        obj.name = obj.name.substr(0, pos).trim();
                    }
                    else if (obj.name[obj.name.length - 1] == ':') {
                        obj.name = obj.name.substr(0, obj.name.length - 1).trim();
                    }
                }

                if (obj.name == "X509v3 extensions") {
                    extensionsLevel = lnLevel;
                }
                else if (extensionsLevel != -1 && lnLevel <= extensionsLevel) {
                    extensionsLevel = -1;
                }

                if (lnLevel == level) {
                    que[1].childs.push(obj);
                    que[0] = obj;
                }
                else if (lnLevel > level) {
                    while (lnLevel > level) {
                        level++;

                        var o = (level == lnLevel ? obj : {childs: []});
                        que[0].childs.push(o);
                        que.unshift(o);
                    }
                }
                else if (lnLevel < level) {
                    que.splice(0, level - lnLevel);
                    level = lnLevel;

                    que[1].childs.push(obj);
                    que[0] = obj;
                }
            }

            function removeEmptyItems(obj) {

                // remove empty sub-objects
                while (obj.childs.length == 1 && !obj.childs[0].name) {
                    obj.childs = obj.childs[0].childs;
                }

                // correct child-objects
                for (var i = 0; i < obj.childs.length; i++) {
                    removeEmptyItems(obj.childs[i]);
                }

            }

            removeEmptyItems(root.childs[0]);
            return root.childs[0];
        }

        function objectizeInfo(obj, ooo) {

            function isBinaryArray(obj) {

                var arr = [];
                for (var i = 0; i < obj.childs.length; i++) {
                    var child = obj.childs[i];
                    if (child.value || child.childs.length > 0) {
                        return null;
                    }

                    for (var c = 0; c < child.name.length; c += 3) {
                        var h = parseInt(child.name.substr(c, 2), 16);

                        if (isNaN(h) || (c < child.name.length - 2 && child.name.substr(c + 2, 1) != ':')) {
                            return null;
                        }
                        arr.push(h);
                    }
                }

                if (arr.length == 0) {
                    return null;
                }

                obj.childs.splice(0, obj.childs.length);
                return arr;
            }

            function isStringArray(obj) {

                var arr = [];
                for (var i = 0; i < obj.childs.length; i++) {
                    var child = obj.childs[i];
                    if (child.value || child.childs.length > 0) {
                        return null;
                    }

                    arr.push(child.name);
                }

                if (arr.length == 0) {
                    return null;
                }

                obj.childs.splice(0, obj.childs.length);
                return arr;
            }

            function isNamedValue(obj) {

                if (obj.childs.length) {
                    if (obj.childs.length == 1 && !obj.value && !obj.childs[0].childs.length && !obj.childs[0].value) {
                        var val = obj.childs[0].name;
                        obj.childs.splice(0, 1);
                        return val;
                    }
                    return null;
                }
                if (!obj.value) {
                    return null;
                }

                return obj.value;
            }

            function convertStringValue(val) {

                function convertSingleValue(val) {
                    if (/^\d+ \(0[xX][0-9a-fA-F]+\)$/.test(val)) {
                        return parseInt(val.substr(0, val.indexOf(' ')));
                    }
                    if (/^\d+$/.test(val)) {
                        return parseInt(val);
                    }

                    var dateVal = Date.parse(val);
                    if (!isNaN(dateVal)) {
                        var date = new Date();
                        date.setTime(dateVal);
                        return date;
                    }

                    return val;
                }

                if (Array.isArray(val)) {
                    var arr = [];
                    for (var i = 0; i < val.length; i++) {
                        arr.push(convertSingleValue(val[i]));
                    }
                    return arr;
                }
                else {
                    return convertSingleValue(val);
                }
            }

            if (obj.name == "Subject Public Key Info" && obj.childs.length == 1 && obj.childs[0].name == "Public Key Algorithm") {
                for (var i = 0; i < obj.childs[0].childs.length; i++) {
                    obj.childs.push(obj.childs[0].childs[i]);
                }
                obj.childs[0].childs.splice(0, obj.childs[0].childs.length);
            }

            var val = isBinaryArray(obj);
            if (val) {
                if (obj.value && obj.name == "Signature Algorithm") {
                    ooo["Signature"] = val;
                    ooo["Signature Algorithm"] = convertStringValue(obj.value);
                }
                else if (obj.value) {
                    ooo[obj.name] = {value: convertStringValue(obj.value), data: val};
                }
                else {
                    ooo[obj.name] = val;
                }
            }

            if (!val) {
                val = isNamedValue(obj);
                if (val) {
                    ooo[obj.name] = convertStringValue(val);
                }
            }

            if (!val) {
                val = isStringArray(obj);
                if (val) {
                    if (obj.value) {
                        ooo[obj.name] = {value: convertStringValue(obj.value), data: convertStringValue(val)};
                    }
                    else {
                        ooo[obj.name] = convertStringValue(val);
                    }
                }
            }

            if (!val) {
                ooo[obj.name] = {};

                if (obj.value) {
                    ooo[obj.name].value = convertStringValue(obj.value);
                }

                for (var i = 0; i < obj.childs.length; i++) {
                    objectizeInfo(obj.childs[i], ooo[obj.name]);
                }
            }
        }

        function findRdns(ooo) {

            function makeRdn(s) {
                var ind = s.indexOf('=');
                if (ind == -1) {
                    return;
                }

                var name = s.substr(0, ind).trim();
                var val = s.substr(ind + 1).trim();

                if (name.length == 0 || val.length == 0) {
                    return;
                }

                return {"rdn": name, "value": val};
            }

            if (Array.isArray(ooo)) {
                for (var i = 0; i < ooo.length; i++) {
                    var o = findRdns(ooo[i]);
                    if (o) {
                        ooo[i] = o;
                    }
                }
            }
            else if (typeof(ooo) == 'object') {
                for (key in ooo) {
                    if (ooo.hasOwnProperty(key)) {
                        var o = findRdns(ooo[key]);
                        if (o) {
                            ooo[key] = o;
                        }
                    }
                }
            }
            else if (typeof(ooo) == 'string') {
                var parts = ooo.split(",");
                var rdn_parts = [];

                for (var ip = 0; ip < parts.length; ip++) {
                    var rdn = makeRdn(parts[ip]);
                    if (!rdn) {
                        return;
                    }

                    rdn_parts.push(rdn);
                }

                if (rdn_parts.length == 1) {
                    return rdn_parts[0];
                }
                else if (rdn_parts.length > 1) {
                    return rdn_parts;
                }
            }
        }

        var root = structurizeCertInfo(info);

        var ooo = {};
        for (var i = 0; i < root.childs.length; i++) {
            objectizeInfo(root.childs[i], ooo);
        }

        if (objectizeRdn) {
            findRdns(ooo);
        }

        return ooo;
    }


})();

///@}
