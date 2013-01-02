$(document).ready(function(){
    $("h2.appNameTitle").click(function(){
        window.location.href = "/";
    });
    
    if (!Modernizr.testAllProps('text-fill-color')) {
    	$("#appTitleContainer").css("color", "#999");
    }
    
});
