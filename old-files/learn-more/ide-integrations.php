<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>IDE Integrations | Architexa</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-learn-more.php') ?>

<script src="/js/jquery-1.3.2.js" type="text/javascript" charset="utf-8"></script>

<link rel="stylesheet" href="/css/prettyPhoto.css" type="text/css" media="screen" charset="utf-8" />
<script src="/js/jquery.prettyPhoto.js" type="text/javascript" charset="utf-8"></script>

<body>

<h1>IDE Integrations</h1>

<p>You need to understand code to do your various tasks - from adding features,
to performance improvements, and fixing bugs. To do these you are usually in
your favorite IDE. In helping understand code Architexa has integrated its tool
suite deeply into the Eclipse IDE. While diagrams can be easily started by
right clicking on a piece of code in an Eclipse view and an Editor, we have
added more integrations based on your needs.</p>

<!-- todo: add package explorer and text editor figure -->


<br>
<h2>Deal with many tabs</h2>

<a href="/images/fullscreen/integration-tabs.png" style="margin-left:5px;float:right" rel="prettyPhoto" title=" is integration-tabs"><img src="/images/thumbnails/integration-tabs.png" width="300" height="200" border="1" alt="View of integration-tabs" /></a>


<p>Often end up with too many editors open in tabs? Seeing developers
frustrations with many tabs being open and having to 'Close All' them,
Architexa allows you to benefit from these open tabs by showing how they are
connected in a single diagram.</p>



<h2 style="clear:both">Speed-up code reviews</h2>

<a href="/images/fullscreen/integration-debugger-pre.png" style="margin-left:5px;float:right" rel="prettyPhoto" title=" is integration-debugger-pre"><img src="/images/thumbnails/integration-debugger-pre.png" width="300" height="200" border="1" alt="View of integration-debugger-pre" /></a>


<p>Next time before you need to get a code-review done, just right click on the
changes go to the 'Team' menu and ask Architexa to analyze the differences and
to create a diagram based on the changes. A diagram pulling in the different
components for a change, should help the reviewer understand the changes faster
and perform the code review more easily.</p>



<h2 style="clear:both">Ease debugging</h2>


<a href="/images/fullscreen/integration-debugger-post.png" style="margin-left:5px;float:right" rel="prettyPhoto" title=" is integration-debugger-post"><img src="/images/thumbnails/integration-debugger-post.png" width="300" height="200" border="1" alt="View of integration-debugger-post" /></a>

<p>Setting breakpoints and debugging is a common way of figuring out complex
bugs. However, Sequence diagrams are expecially usefull in just these cases.
The next time, just right click on the thread in the debugger, and ask
Architexa to create a diagram of what seems to be happening in a sequence
diagram.</p>


<script type="text/javascript" charset="utf-8">
		$(document).ready(function(){
			$("a[rel^='prettyPhoto']").prettyPhoto();

		});
</script>

</body>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
