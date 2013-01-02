
Modernizr.load([
     //animation if css transform not present in ie particularly
    {
    	//css3 stuff we need for animation
    	test	:	Modernizr.cssanimations && Modernizr.csstransforms && Modernizr.csstransitions,
    	
    	//load js file to do animation
    	nope	:	['../scripts/jquery.scrollTo-1.4.2-min.js', 
    	    	 	 '../scripts/home-animation.js']
    }
]);


$(document).ready(function(){
    $("h2.appNameTitle").click(function(){
        window.location.href = "http://market.android.com/details?id=com.bandbaaja";
    });
});