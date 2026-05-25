/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */


$(function () {
    var $header = $('#header'),
        $nav = $('#main_navigation'),
        timeSpan = $('.time > span', $header),
        dateSpan = $('.date > span', $header);

    (function () {
        var date = new Date();
        timeSpan.text(date.toLocaleTimeString());
        dateSpan.text(date.toLocaleDateString());
        setTimeout(arguments.callee, 1000);
    })();

    //Open active menu in navigation
    $('.ui-commandlink.current', $nav).closest('.menu_box').addClass('open');

    //Check screen size and show/hide navigation
    var resizeTimer,
        windowSize = function () {
            if ($nav.outerWidth() + $('#content').outerWidth() <= $(window).width()) {
                $nav.css('left', 0).attr('data-view', 1).addClass('visible');
                $('body').removeClass('hidden_nav');
            } else {
                $nav.css('left', -260).attr('data-view', 2).removeClass('visible');
                $('body').addClass('hidden_nav');
            }
        };
    windowSize();
    $(window).resize(function () {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(windowSize, 50);
    }).scroll(function () {
        var delta = $(this).scrollLeft();
        $('.inner_block', $header).css({left: -delta, right: delta});
    });

    $(document).click(function (e) {
        if ($('.toggle_menu', $header).hasClass('open') && $(e.target).closest('.toggle_menu', $header).size() == 0) {
            $('.toggle_menu', $header).removeClass('open');
        }
    });

    $header.on('click', '.toggle_menu', function () {
        $('.toggle_menu.open', $header).not(this).removeClass('open');
        $(this).toggleClass('open');
    }).on('click', '.nav_toggle', function () {
        if ($nav.hasClass('disable')) {
            return false;
        }
        if ($nav.hasClass('visible')) {
            $nav.css('left', -260).removeClass('visible');
        } else {
            $nav.addClass('disable');
            $nav.addClass('visible').animate({left: 0}, 500, function () {
                $nav.removeClass('disable');
            });
        }
    });

    $nav.on('click', '.menu_box > h4', function () {
        if ($(this).parent().hasClass('open')) {
            $(this).parent().removeClass('open');
        } else {
            $('.menu_box', $nav).removeClass('open');
            $(this).parent().addClass('open');
        }
    }).on('click', ' > h2', function () {
        if ($nav.hasClass('disable')) {
            return false;
        }
        $nav.addClass('disable');
        $nav.css('left', -260).removeClass('visible');
        setTimeout(function () {
            $nav.removeClass('disable');
        }, 500);
    });

    PrimeFaces.widget.DataTable.prototype.getScrollbarWidth = function () {
        if (!this.scrollbarWidth) {
            this.scrollbarWidth = PrimeFaces.env.browser.webkit ? '10' : PrimeFaces.calculateScrollbarWidth();
        }
        return this.scrollbarWidth;
    };

    PrimeFaces.widget.DataTable.prototype.setupScrolling = function () {
        this.scrollHeader = this.jq.children('.ui-datatable-scrollable-header');
        this.scrollBody = this.jq.children('.ui-datatable-scrollable-body');
        this.scrollFooter = this.jq.children('.ui-datatable-scrollable-footer');
        this.scrollStateHolder = $(this.jqId + '_scrollState');
        this.scrollHeaderBox = this.scrollHeader.children('div.ui-datatable-scrollable-header-box');
        this.scrollFooterBox = this.scrollFooter.children('div.ui-datatable-scrollable-footer-box');
        this.headerTable = this.scrollHeaderBox.children('table');
        this.bodyTable = this.cfg.virtualScroll ? this.scrollBody.children('div').children('table') : this.scrollBody.children('table');
        this.footerTable = this.scrollFooter.children('table');
        this.footerCols = this.scrollFooter.find('> .ui-datatable-scrollable-footer-box > table > tfoot > tr > td');
        this.percentageScrollHeight = this.cfg.scrollHeight && (this.cfg.scrollHeight.indexOf('%') !== -1);
        this.percentageScrollWidth = this.cfg.scrollWidth && (this.cfg.scrollWidth.indexOf('%') !== -1);
        var $this = this,
            scrollBarWidth = this.getScrollbarWidth() + 'px';
        if (this.cfg.scrollHeight) {
            if (this.percentageScrollHeight) {
                this.adjustScrollHeight();
            }
            if (this.hasVerticalOverflow()) {
                this.scrollHeaderBox.css('margin-right', scrollBarWidth);
                this.scrollFooterBox.css('margin-right', scrollBarWidth);
            }
        }
        this.fixColumnWidths();
        if (this.cfg.scrollWidth) {
            if (this.percentageScrollWidth)
                this.adjustScrollWidth();
            else
                this.setScrollWidth(parseInt(this.cfg.scrollWidth));
        }
        this.cloneHead();
        this.restoreScrollState();
        if (this.cfg.liveScroll) {
            this.scrollOffset = 0;
            this.cfg.liveScrollBuffer = (100 - this.cfg.liveScrollBuffer) / 100;
            this.shouldLiveScroll = true;
            this.loadingLiveScroll = false;
            this.allLoadedLiveScroll = $this.cfg.scrollStep >= $this.cfg.scrollLimit;
        }
        if (this.cfg.virtualScroll) {
            var row = this.bodyTable.children('tbody').children('tr.ui-widget-content');
            if (row) {
                this.rowHeight = row.outerHeight();
                this.scrollBody.children('div').css('height', parseFloat(this.cfg.scrollLimit * this.rowHeight));
            }
        }
        this.scrollBody.on('scroll.dataTable', function () {
            var scrollLeft = $this.scrollBody.scrollLeft();
            $this.scrollHeaderBox.css('margin-left', -scrollLeft);
            $this.scrollFooterBox.css('margin-left', -scrollLeft);
            if ($this.cfg.virtualScroll) {
                var virtualScrollBody = this;
                clearTimeout($this.scrollTimeout);
                $this.scrollTimeout = setTimeout(function () {
                    var viewportHeight = $this.scrollBody.outerHeight(),
                        tableHeight = $this.bodyTable.outerHeight(),
                        pageHeight = $this.rowHeight * $this.cfg.scrollStep,
                        virtualTableHeight = parseFloat($this.cfg.scrollLimit * $this.rowHeight),
                        pageCount = (virtualTableHeight / pageHeight) || 1;
                    if (virtualScrollBody.scrollTop + viewportHeight > parseFloat($this.bodyTable.css('top')) + tableHeight
                        || virtualScrollBody.scrollTop < parseFloat($this.bodyTable.css('top'))) {
                        var page = Math.floor((virtualScrollBody.scrollTop * pageCount) / (virtualScrollBody.scrollHeight)) + 1;
                        $this.loadRowsWithVirtualScroll(page, ((page - 1) * pageHeight));
                    }
                }, 100);
            }
            else if ($this.shouldLiveScroll) {
                var scrollTop = Math.ceil(this.scrollTop),
                    scrollHeight = this.scrollHeight,
                    viewportHeight = this.clientHeight;
                if ((scrollTop >= ((scrollHeight * $this.cfg.liveScrollBuffer) - (viewportHeight))) && $this.shouldLoadLiveScroll()) {
                    $this.loadLiveRows();
                }
            }
            $this.saveScrollState();
        });
        this.scrollHeader.on('scroll.dataTable', function () {
            $this.scrollHeader.scrollLeft(0);
        });
        this.scrollFooter.on('scroll.dataTable', function () {
            $this.scrollFooter.scrollLeft(0);
        });
        var resizeNS = 'resize.' + this.id;
        $(window).unbind(resizeNS).bind(resizeNS, function () {
            if ($this.jq.is(':visible')) {
                if ($this.percentageScrollHeight)
                    $this.adjustScrollHeight();
                if ($this.percentageScrollWidth)
                    $this.adjustScrollWidth();
            }
        });
    };

    PrimeFaces.widget.DataTable.prototype.updateData = function (data, clear) {
        var empty = (clear === undefined) ? true : clear;
        if (empty) {
            var pageHeight = this.rowHeight * this.cfg.scrollStep;
            var virtualTableHeight = parseFloat((this.cfg.scrollLimit * this.rowHeight) + 'px');
            var pageCount = (virtualTableHeight / pageHeight) || 1;
            var page = Math.floor((this.scrollBody.scrollTop() * pageCount) / (this.scrollBody.get(0).scrollHeight)) + 1;
            this.tbody.html(data);
            this.bodyTable.css('top', ((page - 1) * pageHeight) + 'px');
        } else
            this.tbody.append(data);
        this.postUpdateData();
    };

    PrimeFaces.widget.DataTable.prototype.loadRowsWithVirtualScroll = function (page) {
        if (this.virtualScrollActive) {
            return;
        }

        this.virtualScrollActive = true;

        var $this = this,
            first = (page - 1) * this.cfg.scrollStep,
            options = {
                source: this.id,
                process: this.id,
                update: this.id,
                formId: this.cfg.formId,
                params: [{name: this.id + '_scrolling', value: true},
                    {name: this.id + '_skipChildren', value: true},
                    {name: this.id + '_first', value: first},
                    {name: this.id + '_encodeFeature', value: true}],
                onsuccess: function (responseXML, status, xhr) {
                    PrimeFaces.ajax.Response.handle(responseXML, status, xhr, {
                        widget: $this,
                        handle: function (content) {
                            //insert new rows
                            this.updateData(content);

                            this.virtualScrollActive = false;
                            if (this.hasBehavior('page')) {
                                this.cfg.behaviors.page.call(this, {
                                    source: this.id,
                                    process: this.id,
                                    formId: this.cfg.formId,
                                    params: [{name: this.id + '_pagination', value: true},
                                        {name: this.id + '_first', value: first},
                                        {name: this.id + '_rows', value: this.cfg.scrollStep},
                                        {name: this.id + '_skipChildren', value: true}],
                                    onsuccess: function (responseXML, status, xhr) {
                                        PrimeFaces.ajax.Response.handle(responseXML, status, xhr, {
                                            widget: $this,
                                            handle: function (content) {
                                                this.updateData(content);
                                            }
                                        });

                                        return true;
                                    }
                                });
                            }
                        }
                    });

                    return true;
                },
                oncomplete: function (xhr, status, args) {
                    if (typeof args.totalRecords !== 'undefined') {
                        $this.cfg.scrollLimit = args.totalRecords;
                    }
                }
            };

        PrimeFaces.ajax.Request.handle(options);
    };

    PrimeFaces.widget.SelectOneButton.prototype.unselect = function (button) {
        if (this.cfg.unselectable) {
            button.removeClass('ui-state-active ui-state-hover ui-state-focus').children(':radio').prop('checked', false).change();
            this.triggerChange();
        }
    };

    PrimeFaces.widget.SelectCheckboxMenu.prototype.updateLabel = function () {
        var checkedItems = this.jq.find(':checked'),
            labelText = '';
        if (checkedItems && checkedItems.length) {
            for (var i = 0; i < checkedItems.length; i++) {
                if (i !== 0) {
                    labelText += ', ';
                }
                labelText += $(checkedItems[i]).next().text();
            }
        }
        else {
            labelText = this.defaultLabel;
        }

        this.label.text(labelText);
        this.labelContainer.attr('title', labelText);
    };

    FrontOffice = {
        checkError: function () {
            var errorElement = $('.ui-state-error', $('#content')).eq(0);
            if (errorElement.length) {
                var wnd = $(window),
                    scrollPosition = wnd.scrollTop(),
                    headerHeight = $('#header').find('form').outerHeight(),
                    offsetTop = errorElement.offset().top;
                if ((scrollPosition > offsetTop - headerHeight) || (scrollPosition + wnd.height() < offsetTop + errorElement.height())) {
                    $('html,body').animate({scrollTop: offsetTop - headerHeight - 130}, 'slow');
                }
            }
        },
        addErrorMessage: function (message) {
            $('.message_box .ui-messages').html('<div class="ui-messages-error ui-corner-all"><span class="ui-messages-error-icon"></span>' +
                '<ul><li><span class="ui-messages-error-summary">' + message + '</span></li></ul></div>');
        },
        Token: (function () {
            var instance;
            var init = function () {
                $.ajax({
                    url: "https://localhost:24738/jcext?",
                    method: 'POST',
                    async: false,
                    data: JSON.stringify({jcapi: "ping"})
                }).fail(function () {
                    throw new Error("JC_NOT_INSTALLED");
                });
                var jc = JCWebClient();
                jc.initialize();
                return {
                    authorize: function (signedContent) {
                        if (jc.getLoggedInState()[0] != 0) {
                            jc.unbindToken();
                        }
                        jc.bindTokenUI(0);
                        var certificate = jc.getCertificateList(0)[0][0];
                        return [
                            {
                                name: 'serialNumber',
                                value: jc.parseCertificate(jc.readCertificate(certificate))['Data']['Serial Number']
                            }, {
                                name: 'signature',
                                value: jc.signBase64EncodedData(certificate, btoa(signedContent), false)
                            }
                        ];
                    },
                    onRemoveToken: function (callback) {
                        jc.addEventListener("tokenRemoved", callback);
                    }
                }
            };
            return function () {
                if (!instance) {
                    instance = init();
                }
                return instance;
            }
        })(),
        bindClientSearchBackGuard: function () {
            var $trigger = $('[id$="backToSearchTrigger"]');
            if (!$trigger.length || $trigger.data('foBackGuard')) {
                return;
            }
            $trigger.data('foBackGuard', true);
            if (window.history && history.pushState) {
                history.pushState({foClientSearchGuard: true}, document.title);
            }
            $(window).off('popstate.foClientSearch').on('popstate.foClientSearch', function () {
                if (!$('[id$="backToSearchTrigger"]').length) {
                    $(window).off('popstate.foClientSearch');
                    return;
                }
                $('[id$="backToSearchTrigger"]').click();
            });
        }
    };
});

function focusByPriority() {
    var searchingContext = $('#content').length ? $('#content') : $('#body');
    if (!$('.ui-state-error', searchingContext).length) {
        var highestPriority = Math.max.apply(Math, $('input[focus-priority]:visible, button[focus-priority]:visible', searchingContext).map(function () {
            return parseInt(this.getAttribute('focus-priority'));
        }).toArray());
        var elem = $('input[focus-priority="' + highestPriority + '"]:first, button[focus-priority="' + highestPriority + '"]:first', searchingContext).eq(0);
        if (elem.length) {
            var wnd = $(window),
                scrollPosition = wnd.scrollTop(),
                headerHeight = $('form', $('#header')).outerHeight();
            if ((scrollPosition > elem.offset().top - headerHeight) || (scrollPosition + wnd.height() < elem.offset().top + elem.height())) {
                $('html,body').animate({scrollTop: elem.offset().top - headerHeight - 130}, 'slow', function () {
                    elem.focus();
                });
            } else {
                elem.focus();
            }
            PrimeFaces.customFocus = true;
        }
    }
}
