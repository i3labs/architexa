<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Code Review Diagrams | Architexa</title>
<link rel="stylesheet" href="/css/prettyPhoto.css" type="text/css" media="screen" charset="utf-8" />
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-learn-more.php') ?>

<script src="/js/jquery-1.3.2.js" type="text/javascript" charset="utf-8"></script>

<script src="/js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>




<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/video-and-dl-btns.php') ?>

<h1>
Increase Code Quality with Architectural Review
</h1>


<p>
Code degrades over time. Employees with differing skill levels, coding styles, and familiarity with the architecture will end up lowering the stability of a project. <b>Maintain quality code and prevent architectural degredation</b> with Architexa's integration into Subclipse and CVS. 
</p>

<p>Architexa integrates directly into Eclipse's History and Synchronize views and Eclipse's Team context menu. We support diagram creation for both SVN and CVS repositories. Our diagrams will allow you to simultaneously view changes across multiple files and even projects, realize their impacts or inconsistencies, and quickly document changes to critical components or architecture.</p>

<hr>

<div id = "lmContDiv"> 
	<div id = "lmImageAlign" > 
		<a href="/user-guide/images/0/03/Synchronize.PNG" rel="prettyPhoto" title="Review Code Before Commiting It"><img src="/user-guide/images/0/03/Synchronize.PNG" width="300" height=200 border="1" /></a>
	</div>
		

	<div id = "lmTextAlign" > 
		<h2>Review code before committing it</h2> 
		<p>Under pressure to commit a critical component? Need to quickly check for bugs and inconsistencies before committing it or submitting it for review? Use Architexa to <b>create a diagram of any outgoing changes</b> straight from the synchronize view or the Team context menu. Create a class, sequence, or layered diagram to inspect changes from any angle. <a href="http://www.architexa.com/user-guide/Tasks/Review_your_code_changes_before_committing">Learn How</a>
		</p>
	</div>

</div>

<br>
<hr>

<div id = "lmContDiv"> 
	<div id = "lmImageAlign" > 
	<a href="/user-guide/images/0/07/Changeset-contextmenu.PNG" rel="prettyPhoto" title="Review old change sets"><img src="/user-guide/images/0/07/Changeset-contextmenu.PNG" width="300" height="200" border="1" /></a>
	</div>
	<div id = "lmTextAlign" > 
		<h2>Review old change sets</h2>
		<p> Agile team leads know that even with a disciplined team, it takes a lot of pouring over code in order to prevent bugs from cropping up. Simplify the review process by using Architexa when viewing revisions in Eclipse's History view. You can select any number of revisions to view the affected files and the impact of their modifications on the architecture. <b>Find problem code visually:</b> New dependencies/cycles in a layered diagram, Incorrect inheritance in a class diagram, or faulty logic in a sequence diagram. <a href="http://www.architexa.com/user-guide/Tasks/Review_Team_Members_changes">Learn How</a>

		</p>

	</div>
</div>

<br>
<hr>


<div id = "lmContDiv"> 
	<div id = "lmImageAlign" > 
	<a href="/images/review-diff.png" rel="prettyPhoto" title="Diff Reviews"><img src="/images/review-diff.png" width="300" height="200" border="1" /></a>
	</div>
	<div id = "lmTextAlign" > 
		<h2>Explore diffs in a diagram</h2>
		<p>
			Looking through old commits to find the source of a problem is never a fun task. Diffs can be helpful but it can take a lot of time to figure out where the problem lies. To speed up this process and make code maitence more intuitive, Architexa <b>opens the text of the diff side by side with a visual representation</b>. <a href="http://www.architexa.com/user-guide/Tasks/Track_your_diffs_alongside_diagrams">Learn How</a></p>

	</div>
</div>

<br>
<hr>
<div id = "lmContDiv"> 
	<div id = "lmImageAlign" > 
<a href="/user-guide/images/8/81/Highlight_option.PNG" rel="prettyPhoto" title="Documentation Review"><img src="/user-guide/images/8/81/Highlight_option.PNG" width="300" height="200" border="1" /></a>
	</div>
	
	<div id = "lmTextAlign" > 
		<h2>Documentation review</h2>
		<p>
In addition to making sure the quality of your project's code doesn't degrade over time, it is important to maintain the quality of your documentation. Reviewing the architecture as well as the code itself will ensure a better documented codebase. Developers are often biased to think that their own code is perfectly documented. Another set of eyes can make sure confusing code is documented before it is committed. Finding code that has similar functionality may be difficult without quality architectural documentation. Projects with limited or outdated architectural documentation are more susceptible to code erosion since developers may not be clear on what boundaries they are constrained by.
		</p>
	</div>

</div>

<br>
<hr>



<div id = "lmContDiv"> 
	<div id = "lmImageAlign" > 
<a href="/user-guide/images/1/15/Impact.PNG" rel="prettyPhoto" title="Document Changes"><img src="/user-guide/images/1/15/Impact.PNG" width="300" height="200" border="1" /></a>
	</div>
	
	<div id = "lmTextAlign" > 
		<h2>Document changes and new components</h2>
		<p>
			Has keeping documentation up-to-date become a tedious and time consuming hassle for your team? Using Architexa's team integration features will allow you to <b>create up to date diagrams of critical code components</b> as they are committed. With Architexa you can view, download, and explore existing diagrams and documentation. You can then update these diagrams with new code components. By integrating with Eclipse's History and Synchronize views this process becomes a streamlined step in the code review and committing phase of development. 		<a href="http://www.architexa.com/user-guide/Tasks/Document_the_impact_of_recently_made_changes_to_the_code_architecture">Learn How</a>
		</p>
	</div>

</div>








<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>


<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
