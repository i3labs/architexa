<div id="body-left">

<!--[if IE ]>
<div id="subMenuLeft2" style="margin-top:15px; padding-bottom:20px; border-bottom:1px dotted #ccc; margin-left: -40px;">
<![endif]-->
<!--[if !IE]>-->
<div id="subMenuLeft2" style="margin-top:15px; padding-bottom:20px; border-bottom:1px dotted #ccc;">
<!--<![endif]-->


<ul style="list-style-type:none;padding-left:35px"><li>
	<?php sideMenu("/about/", "index", "Company") ?>
</li><li style="margin-top:10px">
	<?php sideMenu("/about/", "team", "Team") ?>
</li><li style="margin-top:10px">
	<?php sideMenu("/blog", "", "News") ?>
</li>

<li><div id="nested"><ul>
<li style="list-style-type: none;">
	<a href="/blog/category/updates">Updates</a>
</li>

<li style="list-style-type: none;">
	<a href="/blog/category/feedback">Feedback</a>
</li>

<li style="list-style-type: none;">
	<a href="/blog/category/events">Events</a>
</li>

<li style="list-style-type: none;">
	<a href="/blog/category/general">General</a>
</li>

</ul></div></li>



<li style="margin-top:10px">
	<?php sideMenu("/about/", "affiliation", "Our Affiliations") ?>
</li><li style="margin-top:10px">
	<?php sideMenu("/about/", "contact", "Contact Us") ?>

</li></ul>
</div> <!-- end sub-menu -->

<!-- Tech Menu-->

<!--[if IE ]>
<div id="subMenuLeft2" style="margin-top:15px; padding-top:15px; margin-left: -40px;">
<![endif]-->
<!--[if !IE]>-->
<div id="subMenuLeft2" style="margin-top:15px; padding-top:15px;">
<!--<![endif]-->

<ul style="list-style-type:none;padding-left:35px">
<li>
	<?php sideMenu("/about/", "labs/index", "Labs") ?>
</li>

<li style="margin-top:10px">
	<?php sideMenu("/about/", "credits", "Thanks!") ?>
</li>

</ul>
</div> <!-- end Tech menu -->



<div style="margin-top:15px;font-weight:bold">Questions?</div>
<a href="/support">Architexa Support</a>

</div> <!-- end float -->


<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-start.php') ?>

