var onDelete = function () {
	$("form").attr('action', '/delete/gigs');
	$("form").attr('method', 'POST')
	$("form").submit();
};

var onEdit	=	function() {
	//TODO: validate only if one checkbox is selected
	$("form").attr('action', '/edit/gig');
	$("form").attr('method', 'GET')
	$("form").submit();
};

var onPrevious	=	function() {
	$('#goToPrevious').val('goToPrevious');
	$("form").attr('action', '/admin/view');
	$("form").attr('method', 'GET');
	$("form").submit();
};

var onNext	=	function() {
	$('#goToNext').val('goToNext');
	$("form").attr('action', '/admin/view');
	$("form").attr('method', 'GET');
	$("form").submit();
}

$(document).ready(function(){
	$("#delete").click(onDelete);
	$("#edit").click(onEdit);
	$("#previous").click(onPrevious);
	$("#next").click(onNext);
});