function toggle(obj, img) {
	var el1 = document.getElementById(obj);
	var el2 = document.getElementById(img);
	if ( el1.style.display != 'none' ) {
		el1.style.display = 'none';
		el2.src = 'close.png';
	}
	else {
		el1.style.display = '';
		el2.src = 'open.png';
	}
}