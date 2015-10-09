<!--
Note: <b>Beta</b> - Please do not distribute this url.<br>
-->



<ul>
<!--
<a href="/">Home</a>
-->

<!-- menu -->

<!--
<?php //echo "X" . $_SERVER["PHP_NAME"] . "X" ?>
<?php //echo "X" . $_SERVER["SCRIPT_NAME"] . "X" ?>
<?php //echo "X" . $_SERVER["REQUEST_URI"] . "X" ?>
<?php echo "X" . $_SERVER["PHP_NAME"] . "X" ?>
<?php echo "X" . $_SERVER["SCRIPT_NAME"] . "X" ?>
<?php echo "X" . $_SERVER["REQUEST_URI"] . "X" ?>
-->

<?php
$scriptName = $_SERVER["REQUEST_URI"];
function strbeg($string, $search) {
    return (strncmp($string, $search, strlen($search)) == 0);
}
function streq($string, $search) {
    return (strncmp($string, $search, strlen($string)) == 0);
}
?>



<?php if (streq($scriptName, "/")) { ?>
	<li><span> Home </span></li>
<?php } else { ?>
	<li><a href="http://www.architexa.com/">Home</a></li>
<?php } ?>

<li> | </li>

<?php if (strbeg($scriptName, "/learn-more")) { ?>
	<li><span>Learn More</span></li>
<?php } else { ?>
	<li><a href="/learn-more/index">Learn More</a></li>
<?php } ?>


<li> | </li>

<?php if (strbeg($scriptName, "/solutions")) { ?>
	<li><span>Solutions</span></li>
<?php } else { ?>
	<li><a href="/solutions/index">Solutions</a></li>
<?php } ?>

<li> | </li>

<?php if (strbeg($scriptName, "/support") || strbeg($scriptName, "/user-guide") || strbeg($scriptName, "/forums")) { ?>
	<li><span>Support</span></li>
<?php } else { ?>
	<li><a href="/support/index">Support</a></li>
<?php } ?>

<li> | </li>

<?php if (strbeg($scriptName, "/about") || strbeg($scriptName, "/blog")) { ?>
	<li><span>About Us</span></li>
<?php } else { ?>
	<li><a href="/about/index">About Us</a></li>
<?php } ?>

</ul>


