<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Architexa - Know Your Code</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>

<!--
<html>
<head>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
-->

<script type="text/javascript" src="/js/jquery-1.3.2.js"></script>
<script type="text/javascript" src="/js/stepcarousel.js"></script>

<style type="text/css">

.stepcarousel{
	position: relative; /*leave this value alone*/
	border: 0px solid white;
	overflow: scroll; /*leave this value alone*/
	width: 425px; /*Width of Carousel Viewer itself*/
	height: 300px; /*Height should enough to fit largest content's height*/
}

.stepcarousel .belt{
	position: absolute; /*leave this value alone*/
	left: 1;
	top: 2;
}

.stepcarousel .panel{
	float: center; /*leave this value alone*/
	overflow: hidden; /*clip content that go outside dimensions of holding panel DIV*/
	margin: 5px; /*margin around each panel*/
	width: 500px; /*Width of each panel holding each content. If removed, widths should be individually defined on each content DIV then. */
}



.news li {margin:3px 0px 5px -20px;}
.news {
	list-style-position:outside;
	list-style-image: url("/images/news_icon14.png");
	font-size:14px;
}
.date {
	font-style:italic;
	color:gray;
}
.quote {
	font-style: italic;
	margin: 39px 0px 12px 67px;
	padding-left: 20px;
	width: 70%;
}

.testContent {
	background: #FFF;
	border: 1px solid #E4E4E4;
	color: #616161;
	display: table-cell;
	padding: 12px;
	text-align: center;
	z-index: 9;
}
.testQuote{
	font-family: Georgia, Times, serif;
	font-size: 15px;
	padding: 0px;
}
.author{
	color: #9B9B9B;
	font-family: Georgia, Times, serif;
	font-size: 12px;
	font-style: italic;
	padding: 0px;
}
</style>

<!--
</head>


<body>
-->

<script type="text/javascript">
<!--

stepcarousel.setup({
	galleryid: 'mygallery', //id of carousel DIV
	beltclass: 'belt', //class of inner "belt" DIV containing all the panel DIVs
	panelclass: 'panel', //class of panel DIVs each holding content
	autostep: {enable:true, moveby:1, pause:5000},
	panelbehavior: {speed:500, wraparound:true, persist:false},
	defaultbuttons: {enable: true, moveby: 1, leftnav: ['/images/carousel/button-left-rollover.gif', -10-20, 140], rightnav: ['/images/carousel/button-right-rollover.gif', -10+20, 140]},
	statusvars: ['statusA', 'statusB', 'statusC'], //register 3 variables that contain current panel (start), current panel (last), and total panels
	contenttype: ['inline'] //content setting ['inline'] or ['external', 'path_to_external_file']
})
function img(imgID, imgName) {
	document.images[imgID].src = '/images/' + imgName + '.png';
}
// -->
</script>


<table boder="0"><tr>
<td width="55%" valign="center"><div>
	<h1 style="font-size:255%;font-family:Tahoma,Georgia,'Bitstream Vera Serif','Times New Roman',serif">Too Much Code?</h1>

	<h2>Benefit from powerful interactive <br> exploration of code. 
	</h2><br>

	<br>
	<button style="font-size:200%" onclick="window.location='http://www.architexa.com/start/index'">&gt;&gt; Download&nbsp;</button><br>
<span style="font-size:80%">Start with a free 30-day trial.</span>
</div></td>
<td width="5%">&nbsp;</td>
<td height="300">

	<a href="http://www.architexa.com/learn-more/videos/intro/video" onMouseOver="img('play','playIconOn')" onMouseOut="img('play','playIconOff')">

	<div id="mygallery" class="stepcarousel">
	<div class="belt">

	<div class="panel">
		<div style="background-color:white;padding:42px 4px">
			<img border="0" src="/images/class_test2.png" />
		</div>
		<div style="font-weight:bold;font-style:italic;padding:5px;position:relative;top:-290px">See Related Code in <br>Class Diagrams</div>
	</div>

	<div class="panel">
		<div style="background-color:white;padding:18px 50px">
			<img border="0"  src="/images/seq_test4.png" style="width:300px"/>
		</div>
		<div style="font-weight:bold;font-style:italic;padding:5px;position:relative;top:-290px"><br><br><br><br><br><br><br><br>Understand Code Behavior<br> Using Sequence Diagrams</div>
	</div>

	<div class="panel">
		<div style="background-color:white;padding:15px 80px">
			<img border="0" src="/images/layered_test3.png" style="width:250px" />
		</div>
		<div style="font-weight:bold;font-style:italic;padding:5px;position:relative;top:-290px">Get Quick<br> Code<br> Overviews<br> with<br> Layered<br> Architectural<br> Diagrams</div>
	</div>


	</div> <!-- belt -->
	</div> <!--gallery -->

	<div style="position:relative">
		<img name="play" src="/images/playIconOff.png" border="0" style="position:absolute;top:-180px;left:180px"/>
	</div>
	<div style="text-align:center">
		See short video overview (3 min) &nbsp;<img src="/images/PlayIcon1.png" border="0" width="13" height="13"></img>
	</div>
</a>

</td>
</tr></table>
<hr style = "height: 1px">
<table align="center">
	<tr> 
		<td style="width:240px;">
			<div>
			<p style="font-size: 24px; margin:0 0 10px 0;">Powerful Diagrams</p>
			<ul style="font-size:12px; margin: 0pt 0pt 0pt 35px;">
				<li>Layered Diagram Provides the High Level Understanding</li>
				<li>Class Diagram Provides the Interconnection among Elements</li>
				<li>Sequence Diagram Shows the Logic Flow of Code</li>
			</ul>
			</div>
		</td>
		<td style="width:60px;"><img src="/images/Arrow.png" width="60px" style="margin:0 20px 0 0px;"></img></td>
		<td style="width:240px;">
			<p style="font-size: 24px; margin:0 0 10px 0;">Development Cycles</p>
			<ul style="font-size:18px; margin: 0pt 0pt 0pt 35px;">
				<li>Understanding</li>
				<li>Documentation</li>
				<li>Discussion</li>
			</ul>
		</td>
		<td style="width:60px;"><img src="/images/Arrow.png" width="60px" style="margin:0 20px 0 0px;"></img></td>
		<td style="width:240px;">
			<div>
			<p style="font-size: 24px; margin:0 0 10px 0;">Challenges Faced</p>
			<ul style="font-size:18px; margin: 0pt 0pt 0pt 35px;">
				<li>Quality Deterioration</li>
				<li>Agile Development</li>
				<li>Collaboration</li>
			</ul>
			</div>
		</td>
	</tr>
</table>

<hr style = "height: 1px">
<!--
<div class="calloutPara calloutPlus" style="margin-top:50px">
	We are proud of what we have built - <a href="/learn-more">learn more</a> about using it, read about the underlying <a
	href="/technology">technology</a>, and tell us what you think in the <a
	href="/support/forums">forums</a>.
	We are proud of what we have built - read more about our <a
	href="/technology">technology</a> and tell us what you think in the <a
	href="/support/forums">forums</a>.
</div>
-->

<div id="leftcol" style="padding: 10px; float: right; width: 250px;">
		<form name="newsletterAdd" method="post" action="newsletterAdd">
		<table class="testContent" style="padding:10px 4px;"><tr><td>
			Sign up below for monthly updates<br>
			</td></tr><tr><td>
			<input name="referrer" id="referrer" type="hidden" value="<?php echo $_SERVER['HTTP_REFERER']; ?>">
			<input name="email" id="email" type="text" value="Enter your email..." style="color:gray; onclick="this.value='';this.style.color='black';" onfocus="this.value=''; this.style.color='black'; this.onfocus=null;">
			<input name="submit" value="Sign Up!" id="updatesSubmit" type="submit">
		</td></tr></table>
		</form>

<h2>News <span style="font-size:.7em; font-weight:normal;"><a target="_blank" href="http://blog.architexa.com/category/about">(more)</a></span></h2>
<ul class="news">
<?
	// Redirect if email is invalid
	set_include_path(
		get_include_path() . 
		PATH_SEPARATOR . '/home/architexaweb/pear/php'
	);
	require_once "XML/RSS.php";

	$rss =& new XML_RSS("blog-about.xml");
	$rss->parse();
?>

<?
$i=0;
foreach ($rss->getItems() as $item) {
	if($i>=2) continue;
		echo "<li><a href=\"" . $item['link'] . "\">" . $item['title'] . "</a><span class='date'> ". date("F d",strtotime($item['pubdate'])) ."</span></li>\n";	
	$i++;
}
?>
</ul>

<h2>Blog Posts <span style="font-size:.7em; font-weight:normal;"><a target="_blank" href="http://blog.architexa.com">(more)</a></span></h2>
<ul class="news">

<?php
	$rss =& new XML_RSS("blog.xml");
	$rss->parse();
?>

<?
$i=0;
foreach ($rss->getItems() as $item) {
	if($i>=2) continue;	
		echo "<li><a href=\"" . $item['link'] . "\">" . $item['title'] . "</a><span class='date'> ". date("F d",strtotime($item['pubdate'])) ."</span></li>\n";	
	$i++;
}
?>
</ul>


<h2>Affiliations and Awards</h2>
<a href="/about/affiliation"><img border="0" src="/images/agile_logo.png" style="height:47px"></a>
<a href="/about/affiliation"><img border="0" src="/images/IASA_logo.png"  style="width:179px"></a>
<a href="/about/affiliation"><img border="0" src="/images/eclipse_logo.png" style="height:72px"></a>
<a href="/about/affiliation"><img border="0" src="/images/mit100k.png" ></a>
</div>

<div style="padding: 10px; float: left; width: 550px; border-right: 1px solid #CCCCCC"  >
<h1>Painlessly Work with Complex Code</h1>

<!--
<h2>Look at your code in a new light</h2>
<p>Look at code relationships. Understand code behavior. Get overviews in a snap. Our tool enables you to have single and holistic view of your code using Class Diagrams. <a href="http://www.architexa.com/learn-more/class-diagrams"><b>Learn more</b></a></p>

<h2>Easily document and discuss code concepts</h2>
<p>Share code diagrams by just one click.Our tools allows simple and easy collaboration feature of documenting, sharing designs, and speed-up code reviews through our Integration feature.<a href="http://www.architexa.com/learn-more/collaborate"><b> Learn more</b></a></p>
</p>
-->
<!--
<h2>Look at your code in a new light</h2>
-->

<p><span style="font-weight:bold;font-size:1.1em">Get up to speed faster:</span> Look at your code in a new light. Examine code relationships. Understand code behavior. Get overviews in a snap. Prevent code design from deteriorating. Architexa enables you to get a unified view of your code using diagrams.</p>

<p><span style="font-weight:bold;font-size:1.1em">Easily document and discuss code concepts:</span> Share code diagrams with a single click. Add notes and discuss diagrams with team members. Our suite allows simple and easy collaboration while documenting, sharing designs and speeding-up code reviews.</p>
<div style="text-align:center">

<button style="font-size:125%" onclick="window.location='http://www.architexa.com/learn-more/index'">Take a Tour</button><br>
<script src="http://code.jquery.com/jquery-latest.min.js"></script>
		<div style="margin-left: auto; margin-right: auto; height:125px; width:400px; padding-top:18px;" align="center" id="quotes">
			<div id="testimonial-1" class="testContent"> 
				<span class="testQuote">"'surf' the source code through UML diagrams, so that one may be viewing more or less detail as you need. It is not all or nothing, which is usually the case with traditional UML tools."</span><br> 
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
		<span id="nav">
		 <img id="1" onClick="slideTo('testimonial-1')" src="/images/bullet_active.gif"/>
		 <img id="2" onClick="slideTo('testimonial-2')" src="/images/bullet.gif"/>
		 <img id="3" onClick="slideTo('testimonial-3')" src="/images/bullet.gif"/>
		</span>
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
<!--
<div>
<blockquote class="quote">"Sometimes it can be too much work to go back and update diagrams once the coding process is underway, especially when under a tight deadline"</blockquote>
<span style="margin-left:220px;color:gray">-James Sugrue, <a href="http://java.dzone.com/articles/how-architexa-makes-life-easy">JavaLobby</a></span>
</div>
-->	  
</div>

</a></p>

</div>
</body>
</html>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>

