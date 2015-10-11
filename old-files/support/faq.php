<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Frequently Asked Questions | Architexa</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-support-beg.php') ?>


<h1>Frequently Asked Questions</h1>
<h2>Architexa Suite</h2>
<h3 class="question"><a href="/support/faq#">Do you support C/C++?</a></h3>
<div>We currently do have a prototype with C/C++ support. If you are interested in using it, please contact us at <a href="mailto:sales@architexa.com">sales@architexa.com</a>.</div>

<h3 class="question"><a href="/support/faq#">Are there any features of Java you do not support?</a></h3>
<div>We do not yet support enums, generics, or annotations.</div>

<h3 class="question"><a href="/support/faq#">What versions of Eclipse do you support?</a></h3>
<div>We support Eclipse 3.2 and later.</div>

<h3 class="question"><a href="/support/faq#">Do you support other IDEs (Netbeans, IntelliJ, etc)?</a></h3>
<div>We currently only support Eclipse. We are adding support for other IDEs on user <a href="mailto:support@architexa.com">requests</a>.</div>

<h3 class="question"><a href="/support/faq#">What source repositories do you provide deep integrations with?</a></h3>
<div>We currently only provide deep integration with CVS and SVN (subclipse). We are working on adding more support based on user <a href="mailto:support@architexa.com">requests</a>.</div>

<h3 class="question"><a href="/support/faq#">Do you support exporting to UML?</a></h3>
<div>Our main objective is to allow users to integrate Architexa RSE seemlessly with their existing applications. Given that UML is a popular standard we intend to allow exporting to it; our current support is only for the RDF standard.</div>

<h3 class="question"><a href="/support/faq#">When installing Architexa I have accepted the Terms and Conditions but cannot continue the installation process, what is going on?</a></h3>
<div>We have noticed that some versions of Eclipse occasionally encounter this problem during installation. Simply click the "Back" button and make sure the Architexa update site is selected.</div>

<h3 class="question"><a href="/support/faq#">When installing Architexa I get an error that is like "An error occurred during the org.eclipse.equinox.internal.p2.engine.phases.CheckTrust phase". What is wrong?</a></h3>

<div>First, be sure to accept the Architexa certificate: During installation, after clicking Finish in the Install dialog, you will receive a prompt, "Do you trust these certificates?" Be sure to check the box next to "Vineet Sinha; R&D; Architexa" and press OK.


<br><br>If you have accepted this certificate but after clicking Finish in the Install dialog or after accepting the Terms and Conditions, you still receive the same error, it may be the result of a known bug in eclipse. Clicking on the Details button in the error dialog will show you a detailed message like the following:
<blockquote>
An error occurred during the org.eclipse.equinox.internal.p2.engine.phases.CheckTrust phase.
<br>session context was:(profile=epp.package.java, phase=org.eclipse.equinox.internal.p2.engine.phases.CheckTrust, operand=, action=).
<br>Error reading signed content.
<br>The file "D:\eclipse\helios_3.6.1\eclipse\plugins\com.architexa.collab_[version number].jar" does not exist
</blockquote>
<!-- Visit http://update.architexa.com/client/ in a web browser and look at the version number on the release shown there, which is the latest Architexa release. Compare it to the [version number] of the jar in the error above. If the version number of the latest Architexa release version is later than the version number in your error message, your eclipse is trying to get an older version of the software.--> This happens if eclipse is caching some installation files. Try the following steps:
<ul>
<li>Go to Eclipse preferences > Install/Update > Available Software Sites</li>
<li>Select the Architexa plugin location and remove it.</li>
<li>Now go to the Help > Install New Software and Add the update url (http://update.architexa.com/client) and try to install again.</li>
<!--<li>Check that eclipse is not caching anything by confirming that the version number in the installation dialog in eclipse now matches the version number in your browser of the latest release.</li>-->
</ul>
</div>

<h3 class="question"><a href="/support/faq#">I am installing Architexa on a Linux OS. It appears to have installed correctly but I don't see any Architexa options when I open Eclipse.</a></h3>
<div>In the eclipse/plugins directory you should see a number of subfolders beginning with com.architexa.* If these are missing Architexa likely failed to install correctly. The most likely cause of this is a permissions problem. To fix this take a look at Tony R's response on our forum: <a href="/forums/topic/how-to-validate-installation-on-linux#post-85">http://www.architexa.com/forums/topic/how-to-validate-installation-on-linux#post-85</a></div>


<h3 class="question"><a href="/support/faq#">Why is the suite licensed as an annual service?</a></h3>

<div>
We are part of the growing number of companies that are simply fed up with 
those selling developer tools that cost an arm and a leg, only to ask you to 
pay again with every new release. Some of these companies do not provide 
support unless you buy the new product and pay extra for support. We also want to make 
sure that our team is constantly improving the product to keep our users happy. 
With our licensing model we have to listen to our users and keep improving our 
software to make sure that the software doesn't just become shelfware. An 
annual subscription service allows us to have a simple licensing model that 
allows us to have a lower upfront cost to our users and gets them all releases 
and updates.  <!--
We feel that a monthly subscription will allow you to pay a lower upfront cost 
while you see the value of the software. The subscription fee also takes into 
account support costs and allows us to provide a central server for you to 
collaborate and share any diagrams. But most importantly, our goal is to build 
a tool that is useful throughout the coding lifecycle and not just something 
that you would want to use for the first few months on a project - a 
subscription license makes sure we stay aligned to the vision.
-->
</div>

<br>
<h2>Licensing Issues</h2>
<h3 class="question"><a href="/support/faq#">I have a Proxy Server/Firewall set up and I am having trouble installing/validating Architexa. What do I do?</a></h3>
<div> Follow the instructions below: (Make sure you ask your network administrator if your settings should be manual or native)
<br><br>
Eclipse 3.3 and above
<ul>
<li>Open you eclipse IDE and navigate to Window | Preferences.</li>
<li>Then navigate to General->Network Connection</li>
<li>Make ActiveProvider as "Manual" or "Native" as required</li>
<li>If "Manual" option is chosen provide the proxy IP address and port number</li>
<li>Click OK.</li>
</ul>
<br>
Eclipse 3.2
<ul>
<li>Begin by navigating to Window | Preferences .</li>
<li>Click Install/Update .</li>
<li>Check Enable HTTP proxy connection</li>
<li>Enter your proxy host in HTTP proxy host address.</li>
<li>Enter your proxy port in HTTP proxy host port</li>
<li>Click OK.</li>
</ul>
</div>

<h3 class="question"><a href="/support/faq#">Architexa validation fails for me. What do I do? </a></h3>
<div>Unsuccessful validation could result from a bad Architexa installation. If you encountered errors during the installation process, or if exceptions appeared in the Error Log once installation completed, follow these instructions:
<ul>
<li>First uninstall Architexa and close eclipse.</li>
<li>Go to your eclipse directory.</li>
<li>Manually delete the com.architexa.rse_* folder in the \features folder and delete any com.architexa.* and *.architexa.com.* jars and folders in the \plugins folder.</li>
<li>Restart eclipse and install Architexa again.</li>
</ul>
<br>If you are able to install Architexa but still have problems validating please read the next question. </div>

<h3 class="question"><a name="offlineValidation" id="offlineValidation" href="/support/faq#">How do I disable Architexa from validating my account daily?</a></h3>
<div>
Disabling Architexa from validating your account daily will lock the suite to only one system and will not check for updates. To disable Architexa validation please do the following
<ul>
<li>Go to "File | Debug_Architexa | Get Unique Id". This will generate a unique key string for your workspace</li>
<li>Go to <a href="http://my.architexa.com/">my.architexa.com</a> and login and click on 'Settings'</li>
<li>You will then be able to click 'get license key'</li>
<li>Enter the key generated in Eclipse from step 1 and the number of days you would like to disable validation for. (No more than the time your account is valid for)</li>
<li>We will then generate a new password which will override the default settings of trying to connect to the internet and will let you use the suite without validation</li>
<li>Enter this entire string in the password field of the Architexa validation dialog.</li>
</ul>
</div>

<h3 class="question"><a href="/support/faq#">Can a single user purchase Licenses for his/her team?</a></h3>
<div>Any user may purchase one or more licenses. Team members invited to join Architexa can then be added to the plan and assigned licenses at your discretion.</div>


<h3 class="question"><a href="/support/faq#">Do you allow for floating Licences?</a></h3>
<div>Floating Licenses are available upon request. Please contact us at <a href="mailto:sales@architexa.com">sales@architexa.com</a></div>



<h3 class="question"><a href="/support/faq#">What do I do if I see the message "Your email address has not been confirmed"</a></h3>
<div>
This message shows up when you have not confirmed that you have registered Architexa with a valid email address. 
<br>To confirm your email address:  
<ul>
<li>Click on the account confirmation link provided in the welcome email and log in with your email and password</li>
<li>You can also resend the confirmation email by logging in to <a href = "http://my.architexa.com/" target = "_blank">http://my.architexa.com</a> and going to Settings > Manage My Email Addresses > Send Confirmation Message</li>
</ul>
</div>


<br>
<h2>Using the Diagrams</h2>
<h3 class="question"><a href="/support/faq#">What is the meaning of a "Dependency" in a Layered Diagram?</a></h3>
<div>A dependency is equivelant to a call made during execution. If a method has a certain variable in it, then you also have a dependency *to the class of the variable. You can say if Method A will call Method B during its execution, then Method A is "dependent" on Method B. A "double arrow" connecting two blocks means the two will call each other during each execution.</div>
 
<h3 class="question"><a href="/support/faq#">Does "re-organizing" the blocks in the layered diagram change the dependencies?</a></h3>
<div style="display: none;">"Re-organizing" is simply changing how the diagram looks. You are re-organizing the view to better understand it. You are not changing the underlying code or dependencies, just how they are displayed.</div>

<h3 class="question"><a href="/support/faq#">What is the purpose of the "add connection" function in the pallete?</a></h3>
<div>Adding connections is used for documenting a diagram. For example, drawing attention to a specific class or package. It does not change the underlying code or actually "connect" elements.</div>
 
<h3 class="question"><a href="/support/faq#">Why can some code only be opened by certain kinds of diagram(s)?</a></h3>
<div>Projects are too high level and would result in too many items shown in the other diagrams, so we only show them in layered diagrams. Elements such as methods and fields are too low level to be shown in a layered diagram so we omit them. </div>

<h3 class="question"><a href="/support/faq#">Can I use Architexa with jars (java libraries)?</a></h3>
<div>To look into a specific jar with Architexa, you can just right click on the project, go to its properties, and select which jars you would like to include in the Architexa index by selecting them from the 'Architexa Build Path'.</div>

<h3 class="question"><a href="/support/faq#">Why can I add duplicate classes to my diagram?</a></h3>
<div>This is possible because an item can be used in multiple different roles in a code base, for example one instance of a datastore class could exist for indexing search data and another instance for access control.</div>

<h3 class="question"><a href="/support/faq#">What is a layered architectual diagram and how can it help me? Why do relationships in layered diagrams sometimes point upwards?</a></h3>
<div>For more information on Architexa Layered Diagrams see our <a href="/learn-more/layered-diagrams">learn more</a> page.</div>


<br>
<h2>Collaboration Server</h2>
<h3 class="question"><a href="/support/faq#">Who can access diagrams that I share?</a></h3>
<div>Only users that you add to your group. If you want only a subsection to see diagrams, then just create a new group and invite them to it.</div>
 
<h3 class="question"><a href="/support/faq#">Can I have a version of this server running within my companies intranet?</a></h3>
<div style="display: none;">Yes. Contact <a href="mailto:sales@architexa.com">sales@architexa.com</a> and we will help you set it up.</div>

<h3 class="question"><a href="/support/faq#">Why do I need a separate server for sharing?</a></h3>
<div>We think it is best for you to put code documentation and diagrams with your code repository - but most often that is just not possible. We often find developers happy with the documentation when they are writing code. But find it inadequate when reading or using code - we want to allow these code readers/users to be able to document the code and discuss it. If the core team likes it, then the documentation can always be pulled into the code repository.</div>


<h3 class="question"><a href="/support/faq#">What information does Architexa gather when installed? Should I be concerned about the security of my source code?</a></h3>
<div>Absolutely not. We do not collect any information regarding your source code. Additional information is only gathered if you allow it.
<ul>
<li>Source Code - not shared.</li>
<li> Architexa Usage Statistics - shared by default. This can be disabled if needed.</li>
<li>Diagrams:
   <ul>
   <li>shared <b>if</b> you explicitly share on our server my.architexa</li>
   <li>shared <b>if</b> you ask to e-mail <b>and</b> you use the default smtp server</li>
   </ul>
 </ul>
</div>
<br>
<h2>Architexa Index</h2>
<h3 class="question"><a href="/support/faq#">What if Architexa Index is out of sync?</a></h3>
<div> 
A simple resolution to this issue is:
<ul>
<li> In your Eclipse workspace navigate to the ".metadata | .plugins " folder.</li>
<li> Delete the "com.architexa.store" folder present there</li>
<li> Restart eclipse and the suite should create a new repository store.</li>
</ul>
</div>

<!--end of architexa faq-->

<div style="border-top:1px solid #ccc; margin-top:20px;"></div>

<!--start of codemaps faq -->
<h1>Frequently Asked Questions - CodeMaps</h1>

<h2>Basic</h2>

<h3 class="question"><a href="/support/faq#">Is the site free?</a></h3>
<div style="display: none; ">
<p>Yes. You are free to register for an account, browse, create and edit any documentation on CodeMaps. You are also free to create and join any groups. Anything you can do on CodeMaps is free.</p>
</div>

<h3 class="question"><a href="/support/faq#">Do I have to log in?</a></h3>
<div style="display: none; ">
<p>You do not need to log in to browse or request documentation. However, you need to log in to comment, create, edit and vote documentation. You also need to log in to join or create groups.</p>
</div>

<h3 class="question"><a href="/support/faq#">What are the benefits of creating/uploading documentation here?</a></h3>
<div style="display: none; ">
<p>You can easily collaborate with other Open Source developers to document your projects. Documentation created here can be edited by others to improve their quality and accuracy. Architectural diagrams can be easily edited by others on CodeMaps -without having to save images locally and then uploading them again. Documentation can be edited on-the-fly on the site.</p>
</div>

<h3 class="question"><a href="/support/faq#">What kinds of documentation can I create here?</a></h3>
<div style="display: none; ">
	<p><ul><li>Architectural documentation: 
	<a href="/learn-more/layered-diagrams">layered diagrams</a>, 
	<a href="/learn-more/class-diagrams">class diagrams</a> and
	<a href="/learn-more/sequence-diagrams">sequence diagrams</a> can be created easily with CodeMaps.</li>
	<li>You can also upload Javadocs, Code Snippets and additional notes.</li></ul>
	</p>
</div> 

<h3 class="question"><a href="/support/faq#">What kinds of documentation can I <i>not</i> create here?</a></h3>
<div style="display: none; ">
<p>CodeMaps does not support the creation/upload of use-case diagrams and user manuals. If you wish to see more such features, do not hesitate to <a href="mailto:feedback@architexa.com">let us know</a>.</p>
</div> 

<br><h2>Documentation & Diagrams </h2>

<h3 class="question"><a href="/support/faq#">How do I add/create diagrams?</a></h3>
<div style="display: none; "> 
	<p>You can create an architectural diagram straight away in CodeMaps:
		<ul>
		<li> Log in or create an account on CodeMaps.</li>
		<li> Select the project you wish to document.</li>
		<li> Click the "explore" button on CodeMaps and explore the diagrams.</li>
		<li> Save the diagrams by sharing it on CodeMaps.</li>
		For help on the tool suite & how to personalize the diagrams see our <a href="/user-guide/Tools">user guide</a>.
		</ul>
	<br>
	You can also upload architectural diagrams (currently only .png images are supported) that you have previously created:
		<ul>
		<li> Log in or create an account on CodeMaps.</li>
		<li> Select the project you wish to document and follow the link for adding a diagram.</li>
		</ul>
	</p>
</div> 

<h3 class="question"><a href="/support/faq#">How do I add code snippets?</a></h3>
<div style="display: none; ">
	<p>You can add code snippets by creating a note. In the Notes editor, there is a button where you can insert a code snippet.
	<br><img title="Diagrams" border="0" src="/images/snippet.jpg">
	</p>
</div> 

<h3 class="question"><a href="/support/faq#">What if I see problems or errors on the documentation?</a></h3>
<div style="display: none; ">
	<p>If you see errors on the documentation itself, you may edit them directly or give feedback by writing comments. For other errors/problems, contact us at <a href="mailto:feedback@architexa.com">feedback@architexa.com</a> and we will follow up.</p>
	<p>Diagram edits are tracked and saved as different versions. To view previous versions of the diagrams, click the "version" link below the diagrams.</p>
	<br><img title="Diagrams" border="0" src="http://www.architexa.com/images/versions.jpg">
</div> 

<h3 class="question"><a href="/support/faq#">Can other people edit my posts or documentations?</a></h3>
<div style="display: none; ">
<p>All contributions are licensed under <a href="http://creativecommons.org/licenses/by-sa/3.0/">Creative Commons</a>. All content (except user comments) on CodeMaps are collaboratively created, edited and improved upon.</p>
</div> 

<h3 class="question"><a href="/support/faq#">What if I can't find the documentation that I am looking for?</a></h3>
<div style="display: none; ">
<p>You can make a request for the documentation. Be specific on the project and aspects of the project that you wish to see documented.</p>
</div> 


<br><h2>Groups</h2>

<h3 class="question"><a href="/support/faq#">What are the benefits of joining a group?</a></h3>
<div style="display: none; ">
	<p>Users within a project group may receive notifications or news about that project, which will be delivered via e-mails.***</p>
	<p>*** Your e-mail address is safe and secure with us. We do not distribute your contact information to the CodeMaps community or third parties. Group members may only send notification to the Group, which will be sent to the group's internal mailing list.
	</p>
</div> 

<h3 class="question"><a href="/support/faq#">Can I create a closed group only with my project members?</a></h3>
<div style="display: none; ">
	<p>No. All groups on CodeMaps are free for anyone to join.</p> 
	<p>If you wish to create a closed group for your own development teams, you may do so by downloading the engine suite and creating a group in our secure sever using <a href="http://my.architexa.com/">my.architexa</a> account. The suite comes as an extension plug-in for Eclipse IDE. Downloading the suite is free if you are working on Open Source projects. 
	<br><a href="mailto:support@architexa.com">Contact us</a> for the free license and download link.
	</p>
</div> 

<br><h2> Miscellenaeous </h2>

<h3 class="question"><a href="/support/faq#">Can I download the engine?</a></h3>
<div style="display: none; ">
	<p>Yes. The suite can be downloaded as a plug-in extension, integrated within Eclipse IDE for Java. You can download the engine and use it for free, if you are using it for Open Source projects.  </p>
	<p><a href="mailto:support@architexa.com">Contact us</a> for the free license and download link.</p>
</div> 

<h3 class="question"><a href="/support/faq#">Can I upload/have my Open Source project on CodeMaps?</a></h3>
<div style="display: none; ">
	<p>Yes. We are happy to assist in making your Open Source project available in CodeMaps. 
	<br>Simply <a href="mailto:support@architexa.com">contact us</a> with a short description of your project and we will follow up.
	</p>
</div> 

<br><br>
<p>Any other questions? Feel free to e-mail us at <a href="mailto:support@architexa.com">support@architexa.com</a></p>

<script>
jQuery(document).ready(function(){
	$('.question').click(function() {
		$(this).next().toggle('normal');
		return false;
	}).next().hide();
	var myFile = document.location.toString();
	if (myFile.match('#')) { // the URL contains an anchor
	  // click the navigation item corresponding to the anchor
	  var myAnchor = '#' + myFile.split('#')[1];
	  $(myAnchor ).click();
	}
});
</script>



<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
