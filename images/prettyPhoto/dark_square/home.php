<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Architexa</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>


<style type="text/css">

.bullets {

		margin-left:40px;
		font-size: 100%;
		text-align: left;
		}

</style>


<p>We are handful of high energy young engineers working really hard to develop the solution that would make life easier for developers to understand their code base better and revalorize the code understanding methodologies.</p>
 <h2>Our product helps in</h2>

 <div class= "bullets">
<li>Understanding the code relationships and behavior</li>
<li>Documenting  and sharing design</li>
<li>Speed-up code reviews</li>
</div>

<p>We started cooking our tool suite at MIT labs and is currently in Beta which is almost cooked. We will be serving it to the public soon once the garnishing is complete. If you are interested in using the Beta version and learn more about us then please email us at <a href="mailto:info@architexa.com">info@architexa.com</a>.</p>

<p>In the meantime feel free to see what we are upto in our <a href="/labs">labs</a> and read more <a href="/about">about us</a>, our <a href="/services">services</a> and our <a href="/about/team">team</a>.</p><br/><br/>

<div>
<form name="notifyList" method="post" action="notifyList.php">
Sign up for updates:&nbsp;&nbsp;
	<input name="referrer" id="referrer" type="hidden" value="<?php echo $_SERVER['HTTP_REFERER']; ?>">
	<input name="email" id="email" type="text" value="Enter your email..." style="color:gray" onfocus="this.value=''; this.style.color='black'; this.onfocus=null;">
	<input name="submit" value="Send Request" id="updatesSubmit" type="submit">
</form>
<br>








<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
