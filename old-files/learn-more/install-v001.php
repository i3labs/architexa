<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Get Started with Architexa | Architexa</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-learn-more.php') ?>

<!-- Google Website Optimizer Tracking Script -->
<script type="text/javascript">
  var _gaq = _gaq || [];
  _gaq.push(['gwo._setAccount', 'UA-18945881-1']);
  _gaq.push(['gwo._trackPageview', '/0545621509/goal']);
  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
</script>
<!-- End of Google Website Optimizer Tracking Script -->



<? 
$msg = $_GET["msg"]; 
$status = $_GET["status"]; 
?>
<style type="text/css"> 
<!--
	.tableBox {
		padding:10px;
		background-color:#fff;
		border: 2px solid #CCC;
	}
	.details {
		font-size:.9em;
		padding-left:5px;
	}
-->
</style>
<h2 style="width:405px; text-align:left;"><a style="color:grey; font-weight:normal;" href="register">1. Register&nbsp&nbsp>>&nbsp </a>2. Download<a style="color:grey; font-weight:normal;" href="tour">&nbsp&nbsp>>&nbsp 3. Take a Tour</a></h2>

<!-- <div style="color:black;background-color:#ddd;padding:15px; margin-bottom:146px;width:435px;"> 
 -->
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/quotes-inc.php') ?>
<? if (!$status) {?>
	<div style="border:1px;color:red;font-weight:bold" >
     	<?= $msg ?>
	</div>
<? } else { ?>
	<div style="border:1px;color:darkGreen">
     	<?= $msg ?>
	</div>
<? } ?>
<div style=" margin-bottom:152px;width:386px" class="tableBox">
		<p>Achitexa is currently available as a Java plugin for the Eclipse IDE. Use the following update site url to install Architexa from within Eclipse.</p>

			<p>For Eclipse 4.2:<br>
			<b><code style="font-size:1.3em;">http://update.architexa.com/4.2/client</code></b>
			<p>For Eclipse 3.x:<br>
			<b><code style="font-size:1.2em;">http://update.architexa.com/client</code></b></p>
            </p>

            Make sure to have your registration account information available to activate the plugin upon installation. 

<h4 class="header"><a href="">Installation Details (Eclipse 4.2)</a></h4>
		<div class="details">
		<ol> 
                <li> 
                In Eclipse, select <b>Help &gt; Install New Software...</b> In the dialog that appears, click on the <b>Add...</b> button.
                Enter the update site URL into the "Location" text box: <code>http://update.architexa.com/4.2/client</code>. 
                </li> 
                <li> 
                Leave the name text box empty (the name will be retrieved from the update site). Click <b>OK</b>.
                </li> 
                <li> 
                The main pane should update to show the Architexa RSE Client. Select the checkbox next to it. Click <b>Next</b>.
                </li> 
                <li> 
                Review the features that you are about to install. Click <b>Next</b>. Accept the license agreements, click <b>Finish</b>, and restart Eclipse when prompted.
                </li> 
                </ol> 
                <br>
		</div>

		<h4 class="header"><a href="">Installation Details (Eclipse 3.5 - 3.8)</a></h4>
		<div class="details">
		<ol> 
                <li> 
                In Eclipse, select <b>Help &gt; Install New Software...</b> In the dialog that appears, click on the <b>Add...</b> button.
                Enter the update site URL into the "Location" text box: <code>http://update.architexa.com/client</code>. 
                <!--
                In Eclipse, select <b>Help &gt; Install New Software...</b> In the dialog that appears, enter the update site URL into the "Work with" text box:
                <code>http://update.architexa.com/client</code>. Click on the <b>Add...</b> button. -->
                </li> 
                <li> 
                Leave the name text box empty (the name will be retrieved from the update site). Click <b>OK</b>.
                </li> 
                <li> 
                The main pane should update to show the Architexa RSE Client. Select the checkbox next to it. Click <b>Next</b>.
                </li> 
                <li> 
                Review the features that you are about to install. Click <b>Next</b>. Accept the license agreements, click <b>Finish</b>, and restart Eclipse when prompted.
                </li> 
                </ol> 
                <br>
		</div>

		<h4 class="header"><a href="">Older Versions (Eclipse 3.2 - 3.4)</a></h4>
		<div class="details">
			<h3>Eclipse 3.2 (Callisto), 3.3 (Europa), and Eclipse 3.4 (Ganymede)</h3> 
			<ol> 
			<li>In Eclipse, select <b>Help &gt; Software Updates &gt; Find and Install...</b> In the dialog that appears, select <b>Search for new features to install</b> and click <b>Next</b></li> 
			<li>Click <b>New Remote Site</b>. Enter a name for the update site and the following value for the URL: <code>http://update.architexa.com/client</code>. Click <b>OK</b>.</li> 
			<li>Ensure that the newly-added site is checked to indicate that it will be searched for features to install. Click <b>Finish</b>.</li> 
			<li>In the subsequent Search Results dialog, select the checkbox for the Architexa update site. Click <b>Next.</b></li> 
			<li>Accept the license agreements, click <b>Next</b>, click <b>Finish</b>, and finally click <b>Install</b>. Restart Eclipse when prompted.</li> 
			</ol> 

		</div>
		<!--  DE-emphasize for freemium release

		<h4 class="header"><a href="">Installing Architexa's Extended Features</a></h4>
		<div class="details">
			Architexa recently released support for Subclipse Integration to allow for faster and more informative code reviews.
			Please use the following update site to install the extended plugin instead of the one above.<br>
			<b><code style="font-size:1.4em;">http://update.architexa.com/client-extended</code></b>
		</div>
		-->


		<h4 class="header"><a href="">Installing Architexa's Group Server</a></h4>
		<div class="details">
			Architexa also provides collaboration features so that teams can utilize the benefits of our in-house server.
			Please use the following update site to install the Group Server.<br>
			<b><code style="font-size:1.4em;">http://update.architexa.com/group-server</code></b>
		</div>

		<h4 class="header"><a href="">Installing Architexa From Behind a Firewall</a></h4>
		<div class="details">
			If you are unable to use the update site to install Architexa, you can install manually:
			<ol>
			<li>Download the zip file located at <a href="http://update.architexa.com/client/client.zip">http://update.architexa.com/client/client.zip</a>
				<br /><i>(For manually installing the extended features use: <a href="http://update.architexa.com/client-extended/client-extended.zip">http://update.architexa.com/client-extended/client-extended.zip</a>)</i>
				<br /><i>(For manually installing on Eclipse Juno (4.2): <a href="http://update.architexa.com/4.2/client/client.zip">http://update.architexa.com/4.2/client/client.zip</a>)</i>
			</li>
			
			<li>Extract the zip file locally to a folder called "client"</li>
			<li>In Eclipse:
				<ol>
				<li>For Eclipse 3.5+, Help > Install New Software > Add > Local</li>
				<li>For earlier Eclipse versions, Software Updates > Find and Install > Search for new features to install > Help > New Local Site</li>
				<li>Browse to the client-extended folder just created and select it. Enter "Architexa" for Name.</li>
				</ol>
			</li>
			</ol>
		</div>
		<br>
<script>
jQuery(document).ready(function(){
	$('.header').click(function() {
		$(this).next().toggle('normal');
		return false;
	}).next().hide();
});
</script>

		<p>To see the most recent changes and updates check out our <a href="http://www.architexa.com/forums/forum/announcements">Announcements</a> page.</p>
		<p>Please take a look at our <a href="/support/faq">FAQ</a> page and our <a href="/forums">forum</a> if you have any questions or problems.</p>

	<div style="margin-top:25px;float:right;">
		<h4><a href="#" onclick="window.location = 'tour';">Take a Tour</a></h4>
	</div>
</div>

 <!-- Google Website Optimizer Conversion Script -->
<script type="text/javascript">
if(typeof(_gat)!='object')document.write('<sc'+'ript src="http'+
(document.location.protocol=='https:'?'s://ssl':'://www')+
'.google-analytics.com/ga.js"></sc'+'ript>')</script>
<script type="text/javascript">
try {
var gwoTracker=_gat._getTracker("UA-20121784-1");
gwoTracker._trackPageview("/1897503668/goal");
}catch(err){}</script>
<!-- End of Google Website Optimizer Conversion Script -->



<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
