<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Development Lifecycle</title>
<link rel="stylesheet" href="/css/prettyPhoto.css" type="text/css" media="screen" charset="utf-8" />
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-learn-more.php') ?>
<script src="/js/jquery-1.3.2.js" type="text/javascript" charset="utf-8"></script>

<script src="/js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>

<meta HTTP-EQUIV="REFRESH" content="0; url=http://www.architexa.com/learn-more/index">
<div style="float:right">

<div style= "padding:17px 2px 2px 2px;"> 
		<a href="/start/index"><img src="/images/thumbnails/tryArchitexa.png" width="170" height="60"  border="0"/></a>
</div>

<div style= "padding:2px 2px;"> 
		<a href="/learn-more/videos/intro/video"><img src="/images/thumbnails/watchIntro.png" width="170" height="60" border="0"  /></a>
</div>
</div>

<h1>Easier Development Lifecycle</h1>

<p>Architexa has been designed to help in the entire development lifecycle.  
Whether it is understanding already written code, discussing important design 
components, or documenting parts of the architecture for use later, you should 
be able to use Architexa to work on large codebases easily.</p>

<div style="float:right;">
<img src="/images/landing/landing-cycle.png" style="width:300px; margin:15px 0 15px 0;"></img>
</div>

<h4><a href="/learn-more/understand">Understand using Familiar Diagrams</a></h4>
<p>Architexa generates UML diagrams of familiar types directly from you codebase, which allows you to have an intuitive and quicker understanding of your code.</p>

<h4><a href="/learn-more/collaboration">Discuss Design and Code Fragments</a></h4>
<p>Architexa facilitate discussion among team members by making sharing easily. The easy access to shared diagrams provides a better understanding of each others' code, which is essential for code review and maintain module boundaries.</p>

<h4><a href="/learn-more/documentation">Document Code Architecture Easily</a></h4>
<p>Architexa helps you to make effective and useful documentation of your code easily. A better and quicker documentation provides a solid foundation for maintaining code quality and easier collaboration.</p>

<!--

<h1>Tackle Development Challenges</h1>

<p>
Software teams often have significant development challenges as their projects get larger and grow more complex. With 
users demanding 
for increased Agility for team members, Architexa provides developers a tool 
suite designed to help them easily get a single consistent view of their code.

</p>
-->

<br>
<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>

<br>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
