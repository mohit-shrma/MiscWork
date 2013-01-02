var onDelete = function () {
	$("form").attr('action', '/delete/feedbacks');
	$("form").attr('method', 'POST')
	$("form").submit();
};

var onPrevious	=	function() {
	$('#goToPrevious').val('goToPrevious');
	$("form").attr('action', '/admin/viewfeedback');
	$("form").attr('method', 'GET');
	$("form").submit();
};

var onNext	=	function() {
	$('#goToNext').val('goToNext');
	$("form").attr('action', '/admin/viewfeedback');
	$("form").attr('method', 'GET');
	$("form").submit();
}

$(document).ready(function(){
	$("#delete").click(onDelete);
	$("#previous").click(onPrevious);
	$("#next").click(onNext);
});