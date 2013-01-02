var onAsNew = function() {
	$("form").attr('action', '/new/gig');
	$("form").submit();
}

var newGigLoad	=	function() {
	$("#date_time").datetimepicker();
	if ($('#asNew').length > 0) {
		$('#asNew').click(onAsNew);
	}
};

$(document).ready(function(){
	newGigLoad();
});