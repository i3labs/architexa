<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Sequence Diagrams | Architexa</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-learn-more.php') ?>


<script src="/js/jquery-1.3.2.js" type="text/javascript" charset="utf-8"></script>

<link rel="stylesheet" href="/css/prettyPhoto.css" type="text/css" media="screen" charset="utf-8" />
<script src="/js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>


<body>




<div style="float:right">

<div style= "padding:17px 2px 2px 2px;"> 
		<a href="/start/index"><img src="/images/thumbnails/tryArchitexa.png" width="170" height="60"  border="0"/></a>
</div>

<div style= "padding:2px 2px;"> 
		<a href="/learn-more/videos/sequence-diagrams/video"><img src="/images/thumbnails/watchsequence.png" width="170" height="60" border="0"  /></a>
</div>
</div>


<h1>Sequence Diagrams
</h1>


<p>Trying to analyze logic when understanding code is a challenge for developers. As a codebase gets bigger, its complexity also increases, resulting in multiple control flows with logic that is difficult to quickly comprehend.</p>


<p>Sequence diagrams can be an excellent solution to this problem, but tools currently available create 
diagrams that become large and overwhelming when dealing with complex 
code. Architexa lets you create sequence diagrams that present a minimal, manageable amount of information and that you can easily interact with.

<p>Architexa ensures the diagrams remain useful when the code gets complicated by hiding less relevant information, using unobtrusive visual elements to convey ideas, and supporting incremental exploration by the user to expand, collapse, add, or delete components at will.
</p>

<!--div style="margin-left:10px;float:center" >
<a href="/learn-more/videos/sequence-diagrams" rel="prettyPhoto" title=" Sequence Diagram"><img src="/images/thumbnails/seq1.jpg" title="Click to view the Sequence diagram in action" width="350" height="250" border="3" alt="The tool extracting sequence diagram from the code"> <br> [Click here for sequence diagram to be in action ] </a>
</div-->

<div id = "lmContDiv"> 
	<div id = "lmImageAlign" style = "margin-top:0px"> 
		<a href="/images/fullscreen/chrono-conditional-hover.png" rel="prettyPhoto" title="Sequence diagram showing method calls"><img src="/images/thumbnails/chrono-conditional-hover.png" width="300" height="200" border="1" /></a>

	</div>
	<div  id = "lmTextAlign" > 
	
	<p>
		Class instances are shown in a panel along the top of the diagram. Each instance has a lifeline running downward that symbolizes the object's involvement in the sequence being represented. </p>
		
		<p>
		A class's methods and fields appear on the lifeline, and calls or accesses to other members are shown with message lines.
	</p>	
		
<a 
href="/learn-more/videos/sequence-diagrams/index.php"> Click here to see 
sequence diagrams in action.
<img src="/images/PlayIcon1.png" border = "0" width="10" height="10"></img>
</a>
	</div>

</div>
<br>
<hr>

<div id = "lmContDiv"> 
	<div id = "lmImageAlign"> 
		<a href="/images/fullscreen/chrono-exploration.png"  rel="prettyPhoto" title="Explore methods with ease"><img src="/images/thumbnails/chrono-exploration.png" width="300" height="200" border="1" /></a>
	</div>
		
	<div  id = "lmTextAlign" > 
	<h2>Easy Exploration</h2>
		Users can interact with and incrementally explore to build a diagram piece by
		piece. You can drag and drop
		elements into a diagram or make use of navigation aids, which are shown as buttons that indicate the type of items they add to the diagram.</p>
		
	</div>
</div>
<br>
<hr>


<div id = "lmContDiv"> 
	<div id = "lmImageAlign"> 
		<a href="/images/fullscreen/chrono-chained-calls.png"  rel="prettyPhoto" title="Add chained call to diagram"><img src="/images/thumbnails/chrono-chained-calls.png" width="300" height="200" border="1"/></a>

	</div>
		
	<div  id = "lmTextAlign" > 
		<h2>Inlined Method Calls</h2>
		<p>
One way Architexa keeps sequence diagrams manageable is to compact information by default and allow the user to expand components as desired. </p> 
<p>
For example, method calls are shown as inlined when a method is directly called on the return value of another method. This inlined or "chained" method can be expanded by selecting one of the methods in the chain in order to display it separately in the diagram.
</p>
	</div>

</div>
<br>
<hr>

<div id = "lmContDiv"> 
	<div id = "lmImageAlign"> 
		<a href="/images/fullscreen/chrono-conditional-collapse.png" rel="prettyPhoto" title="Conditional blocks shown on mouse-hover"><img src="/images/thumbnails/chrono-conditional-collapse.png" width="300" height="200" border="1"/></a>

	</div>
		
	<div  id = "lmTextAlign" > 
		<h2>Conditional Blocks</h2>
<p>
To demonstrate the possible flow of control that a system could take depending on various conditions or tests, Architexa displays loops, iterations, and conditional statements in sequence diagrams. 
</p>

<p>
An overabundance of information is avoided by displaying loop statements only once and by collapsing all but one of the possible execution paths in an if-else block. </p>
<p>
Method calls in a conditional block are surrounded with a highlight that is only displayed when the mouse is hovered over the block.
</p>
	</div>

</div>
<br>
<hr>

<div id = "lmContDiv"> 
	<div id = "lmImageAlign"> 
		<a href="/images/fullscreen/chrono-backward-messages.png" rel="prettyPhoto" title="Hidden calls shown when moused-over"><img src="/images/thumbnails/chrono-backward-messages.png" width="300" height="200" border="1"/></a>

	</div>
		
	<div  id = "lmTextAlign" > 
<h2>Backward Messages</h2>

<p>
If instances are ordered in such a way that a method call flows from right to left in a diagram, this "backward message" is hidden by default. Its existence is indicated by a highlight on the borders of the involved methods, and the call is only shown on mouse-over. </p> 
<p>
Since time increases from top to bottom and left to right in a sequence diagram, Architexa assumes the illustration of a backward message is not the intention of the diagram and hides it to allow the user to focus on more relevant aspects. </p>
<p>
 Also, hiding the message instead of removing it entirely makes it possible for the user to reorder instances in a diagram while still keeping track of the affected messages.
 </p>
	</div>

</div>
<br>
<hr>

<div id = "lmContDiv"> 
	<div id = "lmImageAlign"> 
<a href="/images/fullscreen/chronoUncommitted.png" rel="prettyPhoto" title="Show uncommitted changes in a diagram"><img src="/images/thumbnails/chronoUncommitted.png" width="300" height="200" border="1"  /></a>
	</div>
	
	<div  id = "lmTextAlign" > 
		<h2>Speed Up Code Reviews</h2>
		<p> When doing code reviews or trying to understand work completed by a colleague or even yourself, sequence diagrams can help you. </p> The Team menu in the context menu contains an option to open the uncommitted changes present in a file, package, or project in a sequence diagram; and when using Eclipse's History view it is possible to open a diagram comparing any two revisions of a file. </p> 
		<p> Architexa analyzes the differences and creates a diagram that shows how they are related and the connections among all changes.
		</p>
	</div>
</div>
<br>
<hr>

<div id = "lmContDiv"> 
	<div id = "lmImageAlign"> 
		<a href="/images/fullscreen/integration-debugger-pre.png" rel="prettyPhoto" title="Create a sequence diagram from the stack trace"><img src="/images/thumbnails/integration-debugger-pre.png" width="300" height="200" border="1"  /></a>
	</div>
		
	<div  id = "lmTextAlign" > 
		<h2>Ease in Debugging</h2>

	<p>	Setting breakpoints and using Eclipse's debugger is a common way of figuring out complex bugs. To make debugging easier and more effective, sequence diagrams have been integrated with the debugger so that when code is run, a diagram showing how the program executes can be generated. 
	</p>
	<p>Simply right clicking on a thread in the debugger's stack trace will give the option to create a diagram that the user can then explore and interact with. The diagram is automatically updated, adding new methods as they are executed.
	</p>
<br><br>
	</div>
</div>
<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>

</body>


<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>