<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Layered Diagrams | Architexa</title>
<link rel="stylesheet" href="/css/prettyPhoto.css" type="text/css" media="screen" charset="utf-8" />
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-learn-more.php') ?>

<script src="/js/jquery-1.3.2.js" type="text/javascript" charset="utf-8"></script>

<script src="/js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>






<div style="float:right">

<div style= "padding:17px 2px 2px 2px;"> 
		<a href="/start/index"><img src="/images/thumbnails/tryArchitexa3.png" width="170" height="60"  border="0"/></a>
</div>

<div style= "padding:2px 2px 2px 2px;"> 
		<a href="/start/index"><img src="/images/thumbnails/watchlayered3.png" width="170" height="60"  border="0"/></a>
</div>
<!--
<div style= "padding:2px 2px;"> 
		<a href="/learn-more/videos/intro/video"><img src="/images/thumbnails/watchIntro.png" width="125" height="48" border="0" /></a>
</div>

<div style= "padding:2px 2px;"> 
		<a href="/learn-more/videos/layered-diagrams/video"><img src="/images/thumbnails/watchlayered.png" width="125" height="48" border="0"  /></a>
</div>
-->
</div>

<h1>
Layered Diagrams
</h1>


<p>
Do you find it hard to understand the architecture of your code? You can easily <b>get an overview of a large codebase</b> using a layered diagram.
</p>

<p>Such overviews are a great place to start understanding a project or component. Architexa layered diagrams group classes based on their directory or package (module) structure and illustrate maximum dependencies while ensuring optimal visibility of diagram elements.</p>

<p>Typically, tools that try to help with code architecture require a significant amount of time to learn to use. Architexa RSE creates layered architecture diagrams in just a single click, saving time and effort.  
<a href="/learn-more/videos/layered-diagrams/index.php"> Click here to see layered diagrams in action.
<img src="/images/PlayIcon1.png" border = "0" width="10" height="10"></img>
</a></p>

<!--div style="margin-left:10px;float:center" >
<a href="/learn-more/videos/layered-diagrams" rel="prettyPhoto" title=" Layered Diagram"><img src="/images/thumbnails/layered1.jpg" title="Click to view the Layered diagram in action" width="400" height="300" border="3" alt="The tool extracting layered diagram from the code"> <br> [<b>Click here too see Layered diagrams in action</b>] </a>

<br>
<br>
</div-->




<hr>
<!--<h2 style="text-align: center">View Codebase Organization</h2>-->
<div id = "lmContDiv"> 
	<div id = "lmImageAlign"> 
		<a href="/images/fullscreen/layered1.jpg" rel="prettyPhoto" title="View high level project architecture"><img src="/images/thumbnails/layered1.jpg" width="300" height="200" border="1" /></a>
	</div>
		
	<div  id = "lmTextAlign" > 
		<h2>View Codebase Organization</h2>
		<p>
		Layered diagrams show an organized view of a codebase based on the 
		dependencies present among its different modules.  
		</p>
		<p>
		Modules displayed in 
		higher layers in a diagram depend on modules in lower layers. Also, modules that contain
                a lot of code are drawn larger in size, making it easier to find what you 
                want to focus on.
			</p>
	</div>
</div>
<hr>

<!--<h2 style="text-align: center">Examine Code Dependencies</h2>-->
<div id = "lmContDiv"> 
	<div id = "lmImageAlign" > 
		<a href="/images/fullscreen/layered2.jpg" rel="prettyPhoto" title="Examine code dependencies for a component"><img src="/images/thumbnails/layered2.jpg" width="300" height="200" border="1" /></a>

	</div>
		

	<div id = "lmTextAlign" > 
		<h2>Examine Code Dependencies</h2> 
<p>
		Our layered diagrams clearly illustrate what code would be affected when a change is made.
		</p>
		<p>
		Dependencies are hidden by default to keep the main structure visible, but simply hovering over or clicking on a module reveals dependencies to and from it. 
</p>
		<p>
		To further emphasize the dependencies, modules are colored in an intuitive 
		shading scheme with the dependee shaded darker than the dependent. 
</p>
<p>
		When 
		cyclical dependencies are found (i.e, when two modules depend on one another) 
		each of them is colored the same.
		</p>
	</div>

</div>
<br>
<hr>
<!--
Architexa RSE automatically removes nodes that are not the source or the
destination of any selected relationship.
-->

<!-- todo: add about breaking and focusing -->
<!-- todo: add screenshot of 'showing upwards arrows' -->
<!-- todo: nav aids on modules -->
<!-- todo: add into general: drag-and-drop, delete an element by selecting it -->

<!--<h2 style="text-align: center">Dive into Details</h2>-->
<div id = "lmContDiv"> 
	<div id = "lmImageAlign" > 
		<a href="/images/fullscreen/layered3.jpg" rel="prettyPhoto" title="Double click on a package to see contained classes"><img src="/images/thumbnails/layered3.jpg" width="300" height="200" border="1" /></a>

	</div>
		

	<div id = "lmTextAlign" > 
		<h2>Dive into Details</h2>
<p>
		When working with high level overviews, it is important to be able to dive into
		details that matter, so Architexa has brought its intuitive exploration to layered 
		diagrams. </p>
		<p>
		Simply double clicking on a module expands it to show the
              contents in a nested layered diagram. 
		</p>
				<p>
				You can then continue to examine the details in the nested layered diagram, or that diagram can be converted to a class or sequence diagram in a single click.
</p>
	</div>

</div>
<br>
<hr>
<!--<h2 style="text-align: center"> Detect Code Cycles</h2>-->

<div id = "lmContDiv"> 
	<div id = "lmImageAlign" > 
		<a href="/images/fullscreen/strata-cycles.png" rel="prettyPhoto" title="Upward arrows showing cycles"><img src="/images/thumbnails/strata-cycles.png" width="300" height="200" border="1" /></a>

	</div>
		

	<div id = "lmTextAlign" > 
		<h2>Detect Code Cycles</h2>
<p>
		Layered diagrams let you figure out what parts of the codebase depend on one another and make it easy to find cycles in your code.
		</p>
		<p>
		Simply moving your mouse over the different modules will reveal cycles by shading  particular modules gray. 
		</p>
		<p>
		Also, the diagram can be set to show upward arrows, which only exist when there are cycles, allowing you to see all cycles at a glance.
		</p>
		<br><br>
	</div>

</div>



<!--
<h2 style="clear:both">Speed-up code reviews</h2>
-->
<!--
<a href="/images/fullscreen/integration-debugger-pre.png" style="margin-left:5px;float:right" rel="prettyPhoto" title="Create a sequence diagram from the stack trace."><img src="/images/thumbnails/integration-debugger-pre.png" width="300" height="200" border="1" alt="View of integration-debugger-pre" /></a>
-->
<!--
<p>Next time before you need to get a code-review done, just right click on the
changes go to the 'Team' menu and ask Architexa to analyze the differences and
to create a diagram based on the changes. A diagram pulling in the different
components for a change, should help the reviewer understand the changes faster
and perform the code review more easily.</p>
-->





<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>

<!-- todo: below is potentially good
Simple interactions with the diagram allow the user to change the layout. Developers can explore and find relevant and potentially crosscutting concerns within the implementation and use them as modules for future explorations.
</p><p>
-->


<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
