
$(document).ready(function() {
	
	var counter = 0;
	
	function update() {
		$('#screenshotsContainer').scrollTo('#screenshot-'+(counter+1), 400);
		$('#descriptionsContainer').scrollTo('#description-'+(counter+1), 400);
		counter++;
		if (counter > 3) {
			counter = 0;
		}
	}
	
	var animator = setInterval(update, 2500);

});

