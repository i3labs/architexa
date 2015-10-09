<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Understand with Familiar Diagrams | Architexa</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-learn-more.php') ?>

<script src="/js/jquery-1.3.2.js" type="text/javascript" charset="utf-8"></script>

<link rel="stylesheet" href="/css/prettyPhoto.css" type="text/css" media="screen" charset="utf-8" />
<script src="/js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>


<h1>Understand with Familiar Diagrams</h1>


<div style="margin-left:5px;float:right" >
<a href="/images/fullscreen/layered1.jpg" rel="prettyPhoto" title="View Project Architecture using Layer Diagrams."><img src="/images/thumbnails/layered1.jpg" width="300" height="200" border="1" /></a>

<br> <br>

<a href="/images/fullscreen/relo.png" rel="prettyPhoto" title="Show Relationships between classes with the Class Diagram."><img src="/images/thumbnails/relo" width="300" height="200" border="1" /></a>

<br> <br>

<a href="/images/fullscreen/chrono-exploration.png"  rel="prettyPhoto" title="Explore methods using the sequence diagram."><img src="/images/thumbnails/chrono-exploration.png" width="300" height="200" border="1" /></a>
</div>
<p>The challenge with making sense of large projects is in connecting related parts of the code together - something that diagrams are great for.</p>

<p>We have built support for the most common types of diagrams created today - 
whether it is Class Diagrams, Sequence Diagrams, or even Layered Diagrams, we 
want you to be able to create them as easily as possible.</p> 

<p>Build diagrams while coding, and go from:
<ul>
<li>Unwieldy multiple tabs to a single class diagram</li>
<li>Stack traces to sequence diagrams.</li>
<li>Large new codebases to layered architecture diagrams.</li>
</ul>
</p>



<p><b>See High-level Overviews Quickly</b> -
Gain mile-high views of your code by using Layered Architectural Diagrams created by a single click. <a href="layered-diagrams"><b>Learn more</b></a></p>

<p><b>Work with multiple pieces of Code Easily</b> -
Benefit from seeing a single Class Diagram of your code and all relevant relationships. <a href="class-diagrams"><b>Learn more</b></a></p>


<p><b>Examine Detailed Code Behavior</b> -
Analyze logic easily by looking at control flow in Sequence Diagrams. <a href="sequence-diagrams"><b>Learn more</b></a></p>



<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>


<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
