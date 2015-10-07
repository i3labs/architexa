<div style="float:left;">

<div style="background:#00375b;color:white;width:175px;margin-top:15px;padding-top:10px;padding-bottom:10px">

<ul style="list-style-type:none;padding-left:35px"><li>

<?php if (strbeg($scriptName, "/about/index")) { ?>
	The Company
<?php } else { ?>
	<a href="/about/index">The Company</a>
<?php } ?>

</li><li style="margin-top:10px">

<?php if (strbeg($scriptName, "/about/team")) { ?>
	Our Team
<?php } else { ?>
	<a href="/about/team">Our Team</a>
<?php } ?>

</li><li style="margin-top:10px">

<?php if (strbeg($scriptName, "/about/association")) { ?>
	Our Association
<?php } else { ?>
	<a href="/about/association">Our Association</a>
<?php } ?>

</li><li style="margin-top:10px">
<?php if (strbeg($scriptName, "/about/contact")) { ?>
	Contact Us
<?php } else { ?>
	<a href="/about/contact">Contact Us</a>
<?php } ?>

</li></ul>
</div> <!-- end sub-menu -->
</div> <!-- end float -->

<!-- 850px total -->
<div style="float:right; width:625px; margin-left: 5px">

