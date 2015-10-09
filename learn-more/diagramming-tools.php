<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Powerful Diagram Tools</title>
<link rel="stylesheet" href="/css/prettyPhoto.css" type="text/css" media="screen" charset="utf-8" />
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-learn-more.php') ?>
<script src="/js/jquery-1.3.2.js" type="text/javascript" charset="utf-8"></script>

<script src="/js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>




<div style="float:right">

<div style= "padding:17px 2px 2px 2px;"> 
		<a href="/start/index"><img src="/images/thumbnails/tryArchitexa.png" width="170" height="60"  border="0"/></a>
</div>

<div style= "padding:2px 2px;"> 
		<a href="/learn-more/videos/intro/video"><img src="/images/thumbnails/watchIntro.png" width="170" height="60" border="0"  /></a>
</div>
</div>

<h1>Powerful Diagramming Tools</h1>

<p>Architexa allows you to build diagrams very easily straight from your code. With an easy-to-use and powerful UI you will be able to see the diagrams of your code that make sense.</p>



<h4><a href="/learn-more/understand">Layered Diagrams</a></h4>

<p>Layered Diagrams are great to get a high-level view of your code. They 
emphasize the packages, classes, the levels between them and the dependencies 
to provide an easy overview of a large code-base.</p>

<p>Architexa allows you to easily get a layered diagram and then just 
double-click to explore the details of any part of the code that you are 
seeing.</p>
<div style="float:right;">
<img src="/images/landing/landing-diagrams.png" style="width:300px; margin:15px 0 15px 0;"></img>
</div>

<h4><a href="/learn-more/collaboration">Class Diagrams</a></h4>

<p>Class Diagrams are helpful in making sense of core components of a project - 
by seeing the code structure. These diagrams emphasize classes, attributes, 
operations, and the relationships between them to provide a quickly 
understanding of the interconnection among elements.</p>

<p>Architexa allows you to open these diagrams from a code editor and then lets 
choose any relationship - whether it is inheritance, method calls, or others - 
and build a diagram by exploring the relationship.</p>

<h4><a href="/learn-more/documentation">Sequence Diagrams</a></h4>

<p>Sequence Diagrams are useful when examining the nitty-gritty details of 
code. They emphasize the interaction between the given classes/objects to help 
you quickly figure out the detailed logic flow of certain parts of the 
code-base.</p>

<p>Architexa lets you quickly build diagrams straight from debug stack-traces, then 
allows you to link to your diagrams with any code selections updating your 
diagram, so that you can examine the control flow of your code more easily.</p>

<br>
<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>

<br>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
