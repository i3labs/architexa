<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Overcoming Architectural Challenges in Agile</title>
<link rel="stylesheet" href="/css/prettyPhoto.css" type="text/css" media="screen" charset="utf-8" />
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-learn-more.php') ?>
<script src="/js/jquery-1.3.2.js" type="text/javascript" charset="utf-8"></script>

<script src="/js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>

<!--<h1>Challenges faced in Agile implementation</h1>
<p>While the strengths of Agile development are well-known, traditional teams have faced challenges in implementing it successfully. 
Teams are often not able to have a consistent architectural view of a project or often have difficulty in adhering 
to the Agile principles.</p> 


<div style="float:right; margin: 5pt 0pt 0pt 0px;"> 
	<a href="/images/thumbnails/Agile-problem.jpg" rel="prettyPhoto" title="Agile problem" > 
	<img style="margin=0 0 0 10px;" src="/images/thumbnails/Agile-problem.jpg" width="300" height="200" border="1"/> 
	</a> 
</div> 

-->

<h1>Architectural Challenges for Agile Projects</h1>

<table style="float:right; margin: 5pt 0pt 0pt 0px;">
<tr>
<td >
<div style="float:right; margin: 5pt 0pt 0pt 0px;"> 
	<a href="/images/thumbnails/architecturalChallenges.jpg" rel="prettyPhoto" title="Architectural challenges" > 
	<img style="margin=0 0 0 10px;" src="/images/thumbnails/architecturalChallenges.jpg" width="300" height="200" border="1"/> 
	</a> 
</div>
</td>
</tr>
<tr>
<td width="300"><p align="center" style=" margin: -12pt 0pt 0pt 0px;"><font size ="2">Architectural challenges</font></p></td>
</tr>
</table>

<p>The difficulty in working with Agile projects is their constantly changing 
code. While UML tools try to provide architectural support, they are limited in 
that their use requires a significant amount of time before teams can see any 
benefits.</p>

<p>With changing code, developers often cannot afford to spend months updating diagrams. Out-of-date UML diagrams prevent developers from 
enjoying its benefits.</p>

<p><b>Rapid code modifications in Agile projects prevent teams from benefitting from a well-defined architecture 
and clear module boundaries</b> resulting in a number of problems.</p> 


<h3>Undetected missing Requirements</h3>

<p>Without a commonly understood set of components for a project shared among 
the team, developers are prevented from thoroughly examining architectural 
issues.  The lack of a well-defined structure for the project prevents 
developers from noticing important missing capabilities during early 
development. Discovery of such missing requirements towards the end of the 
project requires significant time for both redesigning and implementing 
the needed capabilities.</p>

<h3>Lack of Component Reuse</h3>

<p>Component reuse is widely known to be useful in reducing bugs, improving 
productivity, and simplifying maintenance. However, the lack of a shared 
diagrams, prevents developers from getting a complete view of the 
architecture of the codebase, and limits the effectiveness of planning for 
components reuse.</p>

<h3>Difficulty using shared components</h3>

<p>Even with components planned and designed for reuse, unclear module 
boundaries result in developers not understanding the means for such usage. 
Both module builders and users have no guidance as to the appropriate 
dependencies and usage protocol - capabilities might be accidentally hidden 
and/or important dependencies might not be available to those needing to use 
the module.</p>
 
<!--
It is often unclear to both the module builder of all the parameters needed for the module, and to module users as to how to get the parameters. Thus, developers face difficulty in utilizing shared components.

Thus, developers face difficulty in utilizing shared components as their public methods are hidden from the developers' views. -->


<h3>Architectural Erosion and Code Overlap</h3>

<p>When multiple teams work on different parts of a code base and do not have 
an updated view of module boundaries they find it hard to determine the correct 
location for newly implemented features. Code often ends up in incorrect 
locations, resulting in structural erosion and code overlap across different 
modules.  </p>


<h2>Managing the challenges with Architexa</h2>

<p>The Architexa suite has been designed to support developers in making architectural decisions and allow them to benefit from UML diagrams.</p>


<h4>Understanding Code Architecture and Preventing Boundaries Erosion</h4>

<p>Diagrams built straight from the code allow for developers to be connected 
to the code, and high-level overview diagrams like <a 
href="http://www.architexa.com/learn-more/layered-diagrams">Layered 
Diagrams</a> show developers major code modules for developers to dive into.  
Developers can easily determine where newly implemented features should be 
placed and prevent architectural erosion.</p>


<h4>Maintaining Well-Defined Architecture and Module Boundaries</h4>

<p>Architexa allows developers to easily <a 
href="http://www.architexa.com/learn-more/layered-diagrams">get high-level 
overviews</a>, <a href="http://www.architexa.com/learn-more/class-diagrams">see 
definitions of core code components</a>, and <a 
href="http://www.architexa.com/learn-more/sequence-diagrams">examine the 
logic flow of important use-cases</a>. Developers can go into the code 
step-by-step using the module dependencies,  definitions, and capabilities, to 
identify similar components and ensure that the architecture is kept up-to-date 
with the code.</p>



<h4>Having a Consistent Architecture Shared with the Entire Team</h4>


<p> Beyond support for discovering the architecture, Architexa allows 
developers to share these architecture via <a 
href="http://www.architexa.com/learn-more/document">diagrams with added notes 
and comments</a>. These diagrams act as a common reference point for a team and 
allow for developers to access them when needed.</p>





<!-- 

<h4>Class Diagrams</h4>

<p>
Class Diagrams emphasize the classes, attributes, operations, and the relationships between them to provide a quickly understanding of the interconnection among elements. Through these diagrams, developers can explore the relationship between classes, define the boundaries of different modules and check for missing methods or capabilities within each class. 
</p>

<p>
Each developer can then code according to the architectural needs for a specific part of the project with clear class diagrams defining module boundaries. Code duplication could also be avoided as developers will know which features are already available easily through the diagrams.Through the diagrams, essential capabilities are clearly identified and not missed, preventing any unpleasant development surprises such as missing software requirement from occurring in the future.  
</p>

<p>
Sequence Diagrams emphasize the interaction between the given classes/objects to help developers quickly figure out the detailed logic flow of certain parts of the code-base.
</p>

<h4>Code discussion and annotation</h4>



<ul>
<li>
Code Architecture Documentation provides a common ground built straight from the code, allowing team members to identify common components and provide proper documentation so that developers can effectively deploy shared components.
</li>
<li>
Diagrams annotations can be used to provide reference for component reuse and in architectural definition and discussion
</li>
<li>
Module boundaries can be clearly defined through code discussion and annotation with diagrams
</li>
</ul>


<h3>
Meeting  software requirements
</h3>

<p>
Architectural diagrams also 
</p>

<table style="float:right; margin: 5pt 0pt 0pt 0px;">
<tr>
<td>
<div style="float:right; margin: 5pt 0pt 0pt 0px;"> 
	<a href="/images/thumbnails/relo.png" rel="prettyPhoto" title="Clear overview of methods and class" > 
	<img style="margin=0 0 0 10px;" src="/images/thumbnails/relo.png" width="300" height="200" border="1"/> 
	</a> 
</div>
</td>
</tr>
<tr>
<td width="300"><p align="center" style=" margin: -12pt 0pt 0pt 0px;"><font size = 2>Clear overview of methods and class</font></p></td>
</tr>
</table>
<h3>
Better code visibility
</h3>
-->
<!--
<p>
Diagrams allow developers to maintain better architectural tracking and code visibilty. They would be able to 
optimize code with greater code visibilty and understanding of how other code segments work. With better code visibilty, public module methods and interfaces are exposed so that developers understand how to call and utilize shared components effectively.
</p> 
-->

<!--<a href="/technology/Agile-challenge.php"> >>> Next: Overcoming challenges in follwing Agile Principles </a> -->
<br>
<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>

<br>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
