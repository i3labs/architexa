<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>The Technology Behind Architexa</title>
<link rel="stylesheet" href="/css/prettyPhoto.css" type="text/css" media="screen" charset="utf-8" />
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-technology2.php') ?>
<script src="/js/jquery-1.3.2.js" type="text/javascript" charset="utf-8"></script>

<script src="/js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>


<h1>Deep Eclipse Integration</h1>


<p>Architexa works perfectly with the most popular Java IDE: Eclipse.<br> <b>The deep Integration helps programmers to speed up with Architexa quickly and effortlessly</b><br>
</p>

<ul>
<li><b><i>Sophisticated Real-time Code Analysis and Up To Date Code Views</b></i>
</ul>

<div>
	<div style="float: right">
	
	
	</div>
	
	<div style="float: left">
	<p>
	Architexa never stop working in the background when you are coding. We utilize the built-in real-time analysis function in Eclipse to monitor the process and index all the modifications developers have made. As the calculations are done simultaneously while the changes of code are made, updated diagrams are ready to be presented immediately after the developers finishing coding. Thus, as you refresh the diagrams, updated version will be produced very quickly.
	</p>
	</div>
</div>

<ul>
<li><b><i>Open Stack Trace In Diagrams</b></i>
</ul>

<div>
	<div style="float: right">
	
	
	</div>
	
	<div style="float: left">
	<p>
	Architexa provide developers the function to trace the invoking sequence of a particular class by a single click on the Eclipse project explorer. (Add picture) Staring from a chosen class, all invoked classes will be "pushed" into the diagram according to the order of invoking just like a stack. Then, after reaching the end of invoking, classes are "poped" in a reverse order in the diagram to complete the process.
	</p>
	</div>
</div>

<ul>
<li><b><i>Open Diagrams From Tabs</b></i>
</ul>

<div>
	<div style="float: right">
	
	
	</div>
	
	<div style="float: left">
	<p>
	Besides opening diagrams from project explorer, Architexa also allow user to generate diagrams directly from open tabs of codes. By pulling all open tabs together in a single diagram, developers will get an overview of the code they have recently looked at or worked with and see how all the open tabs are related.
	</p>
	</div>
</div>

<div>
<ul>
<li><b><i>Architexa Perspective And Diagram Related Views</b></i> 
</ul>

	<div style="float: right" align="center">
	<img width="300" height="200" boarder="1" src="/images/thumbnails/perspective.png">	
	</div>
	
	<div style="float: right">
	<p>
	In order to provide more flexibility, besides perfect integration with all usual Eclipse perspectives, Architexa also designed our own perspective to make it easier to study code with its related diagrams. Developers can also move the boxes around to make their own setting. With Architexa perspective, developers can study the code with related diagrams right beside it in their own workplace.
	</p>
	</div>
</div>

<ul>
<li><b><i>Linked Environment Exploration With Code Editor</b></i>
</ul>

<div>
	<div style="float: right">
	
	
	</div>
	
	<div style="float: left">
	<p>
	One of the useful functions Eclipse has is tracing of a particular field or method. Now, with Architexa, developers can generate diagrams that represent the tracing process while it is done in Eclipse. By turning on the "linked editor" function, each visited element is added to the diagrams every time users trying to find the next target in the code base. 
	</p>
	</div>
	
</div>
<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>

<br>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
