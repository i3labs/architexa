<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Dealing with Code Quality</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-solutions-beg.php') ?>

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
<h1>Code Quality in the Software Development</h1>

<h4>The underlying cause of reduced code quality and high costs of most software development projects is the Lack of Code Visibility.</h4>



<p>With projects getting larger there is an increasing need for developers to understand new parts of their code-bases. This often extends to code that a developer might have written a few months ago. Without <b><i>visibility</i></b> into the code implementation, it is hard for a developer to thoroughly understand the code.</p>

<div>

	<div style="float:right; margin:-5px 0 -5px 10px">
	<a href="/images/AgileandErosion.png" rel="prettyPhoto" title="Challenges of Software Development Cycle" >
		<img src="/images/AgileandErosion.png" width="300" height="200" border="0"/>
	</a>
	<a href="/images/AgileandErosion.png" rel="prettyPhoto" title="Challenges of Software Development Cycle" >
		<p align="center" style="margin:-5px 0 0 0">Enlarge</p>
	</a>
	</div>	

<p>Insufficient understanding of such code often causes a number of code quality problems: </p>
	<ul>
		<li>While designing local decisions are often made resulting in an inconsistent and brittle architecture,</li>
		<li>While coding in the absence of clear module boundaries results in new code being placed in incorrect modules and results in an erosion of the code architecture, and </li>
		<li>While testing the difficulty in being thorough with the various components results in bugs being missed.</li>
	</ul>
		<p>As a result, code quality often deteriorates as development continues and developers attempt to update the architecture while implementing new features or address existing bugs. With the "Cost of Change" growing rapidly over time, many projects are led to failure.</p>

		<h4>Beyond individual developers, code quality and visibility is a challenge faced by the entire team.</h4> 

		<p>While code reviews are often recognized as one of the most useful 
techniques in improving code quality, doing them effectively requires team 
members to understand and have a good visibility into code written by others. 
Without such deep understanding it is a challenge for developers to take part 
in useful discussions and sharing of both the current implementation and the 
changes made.</p>

		<p>Beyond collaborative needs, in order to help the entire team to gain a deep visibility into each other's code, a strong documentation of individual developer's code is essential. However, traditionally documenting is usually both time-consuming and difficult. Therefore teams' development efficiency can often get limited due to out-of-date and poorly documented and spaghetti-like code.</p>
</div>


<br>

<h1>Gaining Code Visibility with Architexa</h1>

<h4>Architexa helps developers with code quality by improving code visibility through great diagrams using a familiar and visually scalable interface.</h4>

<p>Diagrams used by Architexa are familiar - similar to those often made on 
whiteboards, but have the power of being connected straight to the code. With 
Architexa, teams can easily explore through interactive diagrams:</p>

<ul>
	<li><a href="/learn-more/layered-diagrams">Layered Diagrams</a> emphasize the packages, classes, the levels between them and the dependencies to provide an easily overview of a large code-base.</li>
	
	<li><a href="/learn-more/class-diagrams">Class Diagrams</a> emphasize the classes, attributes, operations, and the relationships between them to provide a quick understanding of the interconnection among elements.</li>
		
	<li><a href="/learn-more/sequence-diagrams">Sequence Diagrams</a> emphasize the interaction between the given classes/objects to help developers quickly figure out the detailed logic flow of certain parts of the code-base.</li>
</ul>

<p>
All diagramming tools provided by Architexa have built-in support for advanced 
visual scalability so that developers can focus on parts of a code-base that is 
relevant to a developer's task. Architexa also allows developers to interact 
with these diagrams so that they can explore the code in ways that are relevant 
to their task and so that they do not get overwhelmed with irrelevant 
information.
</p>

<h4>Architexa helps development teams to document and review code by increasing the overall visibility of the entire teams code.</h4>

<p>Architexa has been built to provide teams with support for:</p>

<ul>

	<li><a href="/learn-more/collaborate">Discussing 
Design and Code Fragments</a> by allowing team members to have precise discussions of code diagrams, enabling in-depth insight into their colleagues' code, and therefore easily performing common tasks like code reviewing.
</li>

	<li><a href="/learn-more/document">Code 
Architecture Documentation</a> providing a common ground
built straight from the code,  allowing team members to design and implement 
new features with better quality.</li>

</ul>


<br>
<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>


<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>


