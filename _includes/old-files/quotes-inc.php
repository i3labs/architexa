<div style="float:right;width:200px">

<script src="http://code.jquery.com/jquery-latest.min.js"></script> 
<div style="text-align:center">
<span id="nav" style="padding-top:10px;"> 
		 <img id="1" onClick="slideTo('testimonial-1')" src="/images/bullet_active.gif"/> 
		 <img id="2" onClick="slideTo('testimonial-2')" src="/images/bullet.gif"/> 
		 <img id="3" onClick="slideTo('testimonial-3')" src="/images/bullet.gif"/> 
</span>  		
<div style="margin-left: auto; margin-right: auto; height:235px; width:175px; padding-top:1px;" align="center" id="quotes"> 
			<div id="testimonial-1" class="testContent"> 
				<span class="testQuote">" 'surf' the source code through UML diagrams, so that one may be viewing more or less detail as you need. It is not all or nothing, which is usually the case with traditional UML tools."</span><br> 
				<span class="author">- Abraham, <a href="http://javahispano.org/contenidos/es/architexa__un_plugin_de_uml_para_eclipse/">JavaHispano</a></span> 
			</div> 
			<div id="testimonial-2" class="testContent"> 
				<span class="testQuote">"Sometimes it can be too much work to go back and update diagrams once the coding process is underway, especially when under a tight deadline"</span><br> 
				<span class="author">- James Sugrue, <a href="http://java.dzone.com/articles/how-architexa-makes-life-easy">JavaLobby</a></span> 
			</div> 
			<div id="testimonial-3" class="testContent"> 
				<span class="testQuote">"New generation UML tools finally coming?"</span><br> 
				<span class="author">- Gabriel Scerbak, <a href="http://twitter.com/The0retico/status/13808214322">Twitter</a></span> 
			</div> 
		</div> 
</div>
</div>
		<script> 
function init() {
 $('#quotes div').hide();
 $('#quotes div:first').show();
}
 
function slide() {
    var $top = $('#quotes div:visible');
    var num = $top.attr('id').split('-').pop();
    var $topNav = $('#'+num);
 
    var $next;
    var $nextNav;
 if($top.next().length > 0) {
 	 $next = $top.next();
	 $nextNav = $topNav.next();
 } else {
       $next = $('#quotes div:first');
       $nextNav = $('#nav img:first');
 }
  $top.fadeOut("slow",function() {
   $next.fadeIn("slow");
 
  });
 
 $topNav.attr("src","/images/bullet.gif");
 $nextNav.attr("src","/images/bullet_active.gif");
}
 
function slideTo(id) {
    var $top = $('#quotes div:visible');
    var num = $top.attr('id').split('-').pop();
    var $topNav = $('#'+num);
 
    var $next =$('#'+id);
    num = $next.attr('id').split('-').pop();
    var $nextNav = $('#'+num);
  $top.fadeOut("slow", function() {
	 $next.fadeIn("slow");
  });
  $topNav.attr("src","/images/bullet.gif");
  $nextNav.attr("src","/images/bullet_active.gif"); 
 
}
 
$(document).ready(function() {
 init();
    setInterval( "slide()", 10000 );
});
</script> 

