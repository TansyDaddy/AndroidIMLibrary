(function(doc, win) {
	var docEl = doc.documentElement,
		resizeEvt = 'orientationchange' in window ? 'orientationchange' : 'resize',
		recalc = function() {
			var clientWidth = docEl.clientWidth;
			if(!clientWidth) return;
			docEl.style.fontSize = 20 * (clientWidth / 375) + 'px';
			// 限定页面最大宽度时使用
			if(clientWidth > 750) {
				docEl.style.fontSize = '40px';
				return;
			}
			// 限定页面最大宽度时使用 end
		};
	if(!doc.addEventListener) return;
	win.addEventListener(resizeEvt, recalc, false);
	doc.addEventListener('DOMContentLoaded', recalc, false);
})(document, window);