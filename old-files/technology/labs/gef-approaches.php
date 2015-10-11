<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Bringing Graphical Apps to the Web | Architexa Labs</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>

<h1>Labs - Bringing Graphical Apps to the Web</h1>

<div style="text-align:justify">

<h2>Approaches</h2>

<p>There are a number of approaches that we considered for bringing graphical applications to the web. These can be organized around two issues:</p>

<p><b>Location of code:</b> It is possible to have most of the code running on 
the client or running on the server.

<ul>
<li>The challenge with running most of the 
code on the server is that highly-interactive features will have high lag times 
for the user. Such features include typical actions that users do in a 
graphical application like drag-and-drop and mouse hovering. We have a 
prototype built using this approach (see below for instructions), but due to 
its limitations additional resource are being focused on the other approaches.</li>

<li>Having most or part of the code running on the client raises the problem of 
code maintaining multiple languages or codebases. We are thus focusing on an 
automated approach for client side code generation to minimize such maintenance 
challenges.</li>
</ul>

</p>

<p><b>Rendering technology</b>: There are three potential rendering 
technologies for running graphical apps on the web. 

<ul>

<li><b>Flash:</b> Currently this seems to be the best option as it is available 
on all platforms and is consistently implemented on all of them.
</li>

<li><b>SVG/VML:</b> SVG is implemented in Firefox, and similar capabilities are 
avaiable on IE via VML. Using it does require implementing a VML compatabilty 
layer for IE. However, with the standard being significant the approach is 
expected to be fairly heavy-weight and therefore slow for some types of 
applications.</li>

<li><b>Canvas:</b> Canvas is a lightweight response to SVG. It has been 
standardized and has been implemented in all modern browsers (including mobile 
devices like phones) except IE. Because of its lightweight nature it is fast. 
However, the compatability layers for IE have been slow, though we do expect 
this to get fast with time. While a Canvas front-end might not make be a good 
approach for right now, we expect it will be a good option the future, and 
expect that it will be easy to move apps built with the Flash interface to 
Canvas.

</ul>

</p>

<h2>Prototype - lightweight flash client</h2>

<p>This work has been done on top of an implementation of draw2d by 
EclipseSource. Architexa added support for GEF on it.  To try out the code:</p>

<ol>
<li>Use Eclipse 3.3 - it has been only tested on it (though it should work on other platforms).</li>
<li>Check out the code from <a href="https://eclipse-gwf.svn.sourceforge.net/svnroot/eclipse-gwf/p1">https://eclipse-gwf.svn.sourceforge.net/svnroot/eclipse-gwf/p1</a>.</li>
<li>You need to download all the projects in the 'app' and the 'web' directory (you will need the 'dsk' directory for running it on the desktop).</li>
<li>You will also need to set your target platform, as is in the zip in the web directory.</li>
</ol>

</div>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
