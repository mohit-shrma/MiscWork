$(document).ready(function(){
	
	$("#date_time").datetimepicker();
	
	var supports_html5_localStorage = (function () {
		  try {
		    return 'localStorage' in window && window['localStorage'] !== null;
		  } catch (e) {
		    return false;
		  }
		})();
	
	//diff of 7 hrs coming
	var convToIndianTime = function(epochSecs) {
		var indianSecs = epochSecs-(7*60*60);
		var indianMs = indianSecs*1000;
		var inDate = new Date(indianMs);
		return ""+(inDate.getUTCMonth()+1)+"/"+inDate.getUTCDate()+"/"+inDate.getUTCFullYear()+" "
				+inDate.getUTCHours()+":"+inDate.getUTCMinutes();
				
	};
	
	
	var checkAndLocalRetrieve = function(key) {
		var val = localStorage.getItem(key);
		if (val !== undefined && val !== null) {
			return val;
		} else {
			return "";
		}
	};
	if (supports_html5_localStorage) {
		
		$("#artist").val(checkAndLocalRetrieve('fbCacheEventName'));
		$("#venue").val(checkAndLocalRetrieve('fbCacheEventLocation'));
		$("#gig_url").val("https://www.facebook.com/event.php?eid="
							+ checkAndLocalRetrieve('fbCacheEventId'));
		$("#description").html(checkAndLocalRetrieve('fbCacheEventDescription'));
		$("#date_time").val(convToIndianTime(checkAndLocalRetrieve('fbCacheEventStartTime')));
		
		var temp = checkAndLocalRetrieve('fbCacheEventPreciseLoc');
		if (temp.length > 0) {
			$("#city_loc").val(temp);
		}
		
		temp = checkAndLocalRetrieve('fbCacheEventLatLong');
		if (temp.length > 0) {
			$("#latlong").val(temp);
		}
	}
	
});