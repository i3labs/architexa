<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Understand with Familiar Diagrams | Architexa</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-learn-more.php') ?>

<script src="/js/jquery-1.3.2.js" type="text/javascript" charset="utf-8"></script>

<link rel="stylesheet" href="/css/prettyPhoto.css" type="text/css" media="screen" charset="utf-8" />
<script src="/js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>


<div style="float:right">

<div style= "padding:17px 2px 2px 2px;"> 
		<a href="/start/index"><img src="/images/thumbnails/tryArchitexa.png" width="170" height="60"  border="0"/></a>
</div>

<div style= "padding:2px 2px;"> 
		<a href="/learn-more/videos/intro/video"><img src="/images/thumbnails/watchIntro.png" width="170" height="60" border="0"  /></a>
</div>
</div>
<h1>Understand with Familiar Diagrams</h1>

<p>The challenge with making sense of large projects is in connecting related parts of the code together - something that diagrams are great for.</p>

<p>We have built support for the most common types of diagrams created today. 
We want you to be able to create layered diagrams, class diagrams, and sequence diagrams as easily as possible.</p> 

<br>
<table style = "text-align:center" >
    <tr>
		<td style="width: 200px;">
		<b>See High-level Overviews Quickly</b> 
		</td>
			
		<td style="width: 200px;">
		<b>Work with Multiple Pieces of Code Easily</b>
		</td>
		
		<td style="width: 200px;">
		<b>Examine Detailed Code Behavior</b>
		</td>
    
	</tr>

	<tr>
		<td style="width: 200px;">
		<a href="/images/fullscreen/layered1.png" rel="prettyPhoto" title="View project architecture using a layered diagram"><img src="/images/thumbnails/layered1.png" width="192" height="128" border="1" /></a>
		</td>
			
		<td style="width: 200px;">
		<a href="/images/fullscreen/relo.png" rel="prettyPhoto" title="Show relationships between classes with a class diagram"><img src="/images/thumbnails/relo.png" width="192" height="128" border="1" /></a>
		</td>
		
		<td style="width: 200px;">
		<a href="/images/fullscreen/chrono1.png"  rel="prettyPhoto" title="Explore methods using a sequence diagram"><img src="/images/thumbnails/chrono1.png" width="192" height="128" border="1" /></a>
		</td>
	</tr>


	<tr  VALIGN=TOP style = "font-size: 80%">
		<td style="width: 200px;">
	  	Gain mile-high views of your code by using layered architectural diagrams created by a single click. <a href="layered-diagrams"><b>Learn more</b></a>
		</td>
			
		<td style="width: 200px;">
		Benefit from seeing a class diagram of your code and all relevant relationships. <a href="class-diagrams"><b>Learn more</b></a>
		</td>
		
		<td style="width: 200px;">
		Analyze logic easily by looking at control flow in sequence diagrams. <a href="sequence-diagrams"><b>Learn more</b></a>
		</td>
		
	</tr>

</table>
<br>
<p>Build diagrams while coding, and go from:
<ul>
<li>Unwieldy multiple tabs to a single class diagram</li>
<li>Stack traces to sequence diagrams</li>
<li>Large new codebases to layered architecture diagrams</li>
</ul>
<br>
</p>




<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>


<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>

