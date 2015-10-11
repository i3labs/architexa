<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Bringing Graphical Apps to the Web | Architexa Labs</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>


<h1>Labs - Bringing Graphical Apps to the Web</h1>

<div style="text-align:justify">


<div class="note">
Note: We are actively working on bringing information here. Feel free to contact us at <a href="mailto:info@architexa.com">info@architexa.com</a> for more information.
</div>

<p><b>Approach:</b> There are a number of approaches for bringing graphical-apps to the web. We discuss a number of them <a href="/labs/gef-approaches">here</a>. For the reasons discussed there, our primary focus is in using a Java to Flash Compiler for the main app and providing an appropriate set of libraries around for the graphical app.</p>

<p>See below for using challenges, instructions, and running the demo.<p>

<div class="floatImage">
<a href="/labs/images/gef-logic-ful001.png">
<img style="border:0" src="/labs/images/gef-logic-ful001.png" width="100%"><br>
</a>
</div>

<p><b>Demo:</b> The demo is currently working on a web-browser, but for some reason it is bound to localhost. One this is resolved it will be here. See below for downloading, building, and running the demo.<p>

<p><b>Challenges:</b></p>

<p>There are two main challenges that need to be considered for succesfully moving a GEF based app to run on the web:</p>
<ul>

<li>Client/Server Functionality: By default the approach compiles the entire Java code to 
ActionScript for running in the Flash VM. While a good starting point, there are issues that needs to be considered and properly designed around.
<ul>

<li>Download Size: By default, this can result in a this can result in a large 
download size, easily approaching 2MB. A team building on this approach will 
need to carefully consider which functionality would need to run on the server.  
We are working on a set of guidelines and promising techniques for helping 
reducing this size.</li>

<li>Location of User Information: When considering users files and preferences, 
decisions need to be made as to the approach for storing them on the client or 
on the server.  </li>

<li>Library Depenencies in Flash: While we have provided a significant amount 
of the Java Class Library to run in Flash, not everything makes sense. If you 
find some functionality that you need, you will need to either reimplement it 
using the flash counter-parts or work around it.  A similar consideration or 
decision will need to be made for and other java libraries or projects that you 
depend on.</li>

</ul>

</li>

<li>Compiler Bugs: While we have tested the compiler on significant amounts of 
code, there might be small bugs that a new code base might raise in the 
compiler.  Any approach using this will need to go through a testing process. Fortunately, the <a href="http://download.eclipse.org/e4/downloads/drops/S-0.9M1-200902061045/e4-news-M1.html#actionScriptDE">ActionScript Devleopment Environment</a> in Eclipse provides very good debugger support.</li>

</ul>

<p><b>Getting the demo running:</b></p>
<ol>
<li>The GEF functionality builds on the SWT for Flex code. Follow the instructions there, and run the demos to test your setup. The instructions can be found <a href="http://wiki.eclipse.org/E4/SWT/Running_the_demos">here</a>.</li>
<li>We have added a small amount of code to the SWT Class Library. Apply this patch: <a href="swt-e4-jcl_patch.txt">patch</a>.
<li>Check out the code from <a href="https://eclipse-gwf.svn.sourceforge.net/svnroot/eclipse-gwf/p2/fmwk">https://eclipse-gwf.svn.sourceforge.net/svnroot/eclipse-gwf/p2/fmwk</a> and <a href="https://eclipse-gwf.svn.sourceforge.net/svnroot/eclipse-gwf/p2/app">https://eclipse-gwf.svn.sourceforge.net/svnroot/eclipse-gwf/p2/app</a>.</li>
<li>Run LogicLauncher or ShapesLauncher from the org.eclipse.swt.e4.examples project either as a Java app for running on the desktop or as an ActionScript app for a web based version.
</ol>

<p><b>Porting your app:</b></p>

<ol>
<li>Make sure that you have an answer to the challenges above.</li>
<li>Get the demo's running. Our goal was to minimize changes to the original GEF plugins. Therefore, the secret to getting it running is the part of the code in the org.eclipse.swt.e4.examples projects.</li>
<li>Get your app running on the desktop with minimum dependencies to the workbench. We use a set of *Widget classes instead of the GEF *Editor classes to be the integration point for GEF with minimized dependencies. Make sure to create your *Widget class, and ensure you have implementations to configureGraphicalViewer and createPaletteViewerProvider.</li>
<li>Once you have the app running on the desktop, your should be able to get it to cross compile and run on the web. You might face compilation or runtime errors the first time you are testing each piece of functionality.</li>
</ol>


</div>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
