<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>The Technology Behind Architexa</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-learn-more-beg.php') ?>






<script src="/js/jquery-1.3.2.js" type="text/javascript" charset="utf-8"></script>

<script src="/js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>


<div style="float:right; padding-right:20px;">

<div style= "padding:17px 2px 2px 2px;"> 
		<a href="/start/index"><img src="/images/getStarted2.jpg" width="170" height="55"  border="0"/></a>
</div>

<div style= "padding:2px 2px;"> 
		<a href="/learn-more/videos/intro/video"><img src="/images/thumbnails/watchIntro.png" width="170" height="60" border="0"  /></a>
</div>
</div>
<h1>Deep Eclipse Integration</h1>


<script src="/js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>

<p>Architexa has been built to work perfectly with the most popular Java IDE: Eclipse. <b>The deep integration helps programmers to speed up with Architexa quickly and effortlessly.</b> There are many parts to this deep integration.
</p>

<br>

<div style="margin:0px 0 0px 0";>

<h2>Sophisticated real-time code analysis</h2>

	<div>
	<p style="margin: 0 0 0 0px";>
	Architexa works in the background when you are coding. We utilize the built-in real-time analysis capabilities in Eclipse to monitor the coding process and indexes all the modifications you have made. As the calculations are done simultaneously while the changes to the code are made, updated diagrams are ready to be presented immediately after you have finished coding. Thus, as you refresh the diagrams, updated versions will be produced very quickly.
	</p>
	</div>
</div>
<br>
	
<!--Sophisticated Real-time Code Analysis-->

<div style="margin:0px 0 0px 0";>

<h2>Linked environment exploration with code editor</h2>

	<p style="margin: 0 0 0 0px;">
	One popular feature of Eclipse is 'linking' (the tracking of user selections). With Architexa, you can generate diagrams that represent this linking showing diagrams being built while the selections change in Eclipse. By turning on the "linked editor" function, each visited element is added to the diagrams, representing a visual history of what you have seen, while you explore to the next target in the code base. 
	</p>	

</div>
<br>

<!--Linked Environment Exploration With Code Editor-->

<div style="margin:0px 0 10px 0";>

<h2>An easy-to-use Eclipse perspective</h2>

	<div style="float:right; margin: 5pt 0pt 0pt 0px;">
	<a href="/images/thumbnails/perspective.png" rel="prettyPhoto" title="Architexa Perspective" >
	<img style="margin=0 0 0 10px;" src="/images/thumbnails/perspective.png" width="300" height="200" border="1"/>
	</a>
	</div>
	
	<div>
	<p style="margin: 0 10px 0 0px";>
	Architexa provides a central location for code understanding related tasks. Besides deep integration with most Eclipse views and editors, Architexa also has our own perspective designed to simplify examining code with diagrams. The perspective allows you to easily explore code and discuss important design elements with co-workers. You can also move around views and customize the perspective based on their needs. 
	</p>
	</div>

</div>
<br>

<!--Architexa Perspective And Diagram Related Views-->

<div>
	
	<h2 style="margin:20px 0 0px 0";>Open diagrams from anywhere in Eclipse</h2> 
	<p>With its deep integration, Architexa allows you to create diagrams easily from almost anywhere in Eclipse according to your needs. Following are some highlighted examples:</p>
	<table border="0" align="center">
	<tr>
	<td>	
		<a href="/images/thumbnails/integration-tabs.png" rel="prettyPhoto" title="Stack Trace" >
		<img src="/images/thumbnails/integration-tabs.png" width="300" height="200" border="1"/>
		</a>
	</td>
	<td>
		<a href="/images/thumbnails/integration-debugger-pre" rel="prettyPhoto" title="Diagrams from Tab" >
		<img src="/images/thumbnails/integration-debugger-pre" width="300" height="200" border="1"/>
		</a>
	</td>
	</tr>
	<tr>
	<td>	
		<p style="margin: -10px 0 0px 0px;" align="center">
			<b>From tabs to structural Information</b>
		</p>
		<p style="margin: 0px 0pt 20px 0px; font-size: 14px;" align="center">
			Architexa also allows you to generate diagrams directly from open tabs of codes to get an overview of the code you have recently looked at or worked with.
		</p>
		
	</td>
	<td>
		<p style="margin: -10px 0 0px 0px;" align="center">
			<b>From stack traces to sequence diagrams</b>
		</p>
		<p style="margin:0px 0pt 20px 0px; font-size: 14px;" align="center">
			Architexa provides you with the feature to trace the invoking sequence of a particular method with a single click on the Eclipse debugging perspective.
		</p>
	</td>
	</tr>
	<tr>
	<td>	
		<a href="/images/thumbnails/searchdiagram.png" rel="prettyPhoto" title="Diagrams from Search Result" >
		<img src="/images/thumbnails/searchdiagram.png" width="300" height="200" border="1"/>
		</a>
	</td>
	<td>
		<a href="/images/thumbnails/teamScreen.png" rel="prettyPhoto" title="Code Review" >
		<img src="/images/thumbnails/teamScreen.png" width="300" height="200" border="1"/>
		</a>
	</td>
	</tr>
	<tr>
	<td>	
		<p style="margin: -10px 0 0px 0px;" align="center">
		<b>From search results to target diagrams</b>
		</p>
		<p style="margin:0px 0pt 20px 0px; font-size: 14px;" align="center">
		Architexa allows you to generate diagrams from search result directly so that diagrams can be created without extra work.
		</p>
	</td>
	<td>
		<p style="margin: -10px 0 0px 0px;" align="center">
		<b>From code changes to code review diagrams</b>
		</p>
		<p style="margin:0px 0pt 20px 0px; font-size: 14px;" align="center">
		Architexa helps generate diagrams specifically for code changes, which makes it easier for others to review the changes and modifications.
		</p>
	</td>
	</tr>
	</table>
</div>



<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>



<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>


