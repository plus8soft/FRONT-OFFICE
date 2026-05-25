/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */


function translit(a){var b=function(){function b(a){var b=[function(a,b){e[b]=a,d.push(b)},function(a,b){e[a]=b,d.push(a)}][a];return function(a,c){var d=a[0];d&&b(d,c)}}return a=a.replace(/(i(?=.[^аеиоуъ\s]+))/gi,"$1`"),[b(0),function(a){return a.replace(/i``/gi,"i`").replace(/((c)z)(?=[ieyj])/gi,"$1")}]}(),c={"щ":["shh"],"я":["ya"],"ё":["yo"],"ю":["yu"],"ж":["zh"],"ч":["ch"],"ш":["sh"],"э":["e`"],"ъ":["``"],"ы":["y`"],"ц":["cz"],"а":["a"],"б":["b"],"в":["v"],"г":["g"],"д":["d"],"е":["e"],"з":["z"],"и":["i"],"й":["j"],"к":["k"],"л":["l"],"м":["m"],"н":["n"],"о":["o"],"п":["p"],"р":["r"],"с":["s"],"т":["t"],"у":["u"],"ф":["f"],"х":["h"],"ь":["`"]},d=[],e={};for(var f in c)Object.hasOwnProperty.call(c,f)&&b[0](c[f],f);return b[1](a.replace(new RegExp(d.join("|"),"gi"),function(a){var b=a.toLowerCase();if(b===a)return e[a];var c=e[b];return c.charAt(0).toUpperCase()+c.slice(1)}))}
