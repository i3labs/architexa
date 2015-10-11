<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Tackling Development Challenges with Architexa</title>
<link rel="stylesheet" href="/css/prettyPhoto.css" type="text/css" media="screen" charset="utf-8" />
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-solutions.php') ?>
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

<h1>Diagramming and Code Browsing Support for C/C++</h1>

<p>Now you can more easily explore complex C/C++ code bases.  Architexa allows you to create and share
	diagrams and code comments.  We are psyched to bring the power of Architexa diagramming and code browsing to
    the C/C++ development communities!</p>

<!--
<div style="float:right;">
<img src="/images/landing/landing-diagrams.png" style="width:300px; margin:15px 0 15px 0;"></img>
</div>
-->
<!--this is the diagram part-->
<table>
<tr>
	<td>
<div style="float:right; margin:0px 20px 0px 0;">
<a href="/images/AgileandErosion.png" rel="prettyPhoto" title="Challenges of Software Development Cycle" >
	<img src="/images/AgileandErosion.png" width="150" height="100" border="0"/>
</a>
</div>
</td>
<td>
<h4><a href="/solutions/code-quality">Code Quality due to Code Visibility</a></h4>
<p>One of the most important causes of code quality deterioration in most 
projects is lack of code visibility. Architexa provides a deep and precise view 
into your code by easily letting users interact with graphical representations 
of your code. <a href="/solutions/code-quality">Learn More</a></p></td>
</tr>
</table>

<table>
	<tr><td>
<div style="float:right; margin:10px 20px 0px 0;">
<a href="/images/Collaboration-P.png" rel="prettyPhoto" title="Challenges of Software Development Cycle" >
	<img src="/images/Collaboration-P.png" width="150" height="100" border="0"/>
</a>
</div></td>
<td>
<h4><a href="/solutions/distributed-teams">Development in Distributed Teams</a></h4>

<p>As teams are getting increasingly distributed, collaborations needs among 
teams is rapidly increasing.  Beyond easily communicating across the team, 
issues like security and auditing, need to be addressed for development 
projects. Architexa provides development teams with a platform for reducing the communication overhead and improving the success of distributed development. <a 
href="/solutions/distributed-teams">Learn More</a></p></td>
</tr>
</table>

<table>
	<tr>
		<td>
<div style="float:right; margin:10px 20px 0px 0px;">
<a href="/images/thumbnails/architecturechallenge.png" rel="prettyPhoto" title="Challenges of Software Development Cycle" >
	<img src="/images/thumbnails/architecturechallenge.png" width="150" height="100" border="0"/>
</a>
</div>
</td>
<td>
<h4><a href="/solutions/architecture">Maintaining Architecture in Agile Projects</a></h4>

<p>Architecture of projects using Agile development often suffers as module 
boundaries loose their definition over time. This architectural erosion results 
in the lack of component reusing, excessive code duplication and difficulty of 
using the shared components.  By providing UML diagrams built straight from 
the code, Architexa provides a solution for teams struggling with being agile 
and maintaining a consistent architecture. <a 
href="/solutions/architecture">Learn More</a></p></td>
</tr>
</table>

<br>
<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>

<br>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
