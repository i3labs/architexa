<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-technology.php') ?>

<meta HTTP-EQUIV="REFRESH" content="0; url=http://www.architexa.com/about/credits">
<h1>Credits</h1>

We care about helping developers understand code. Since 1999 we have been 
building prototypes. We want to acknowledge some of the inspirations and great 
places where we have had the opportunity to experiment with these ideas. A 
number of people have also been available to get us help and gave us great 
feedback on the technologies built here. Below are a list of such organizations 
and people.

<h2>Organizations</h2>

Some of the organizations that have inspired us or sponsored the development of technology here:
<ul>
<li><a href="http://msdn.microsoft.com/en-us/library/ws8s10w4.aspx">MFC Hierarchy Charts</a><br>
Microsoft Foundation Classes came with a Hierarchy Chart that provided the initial inspiration and showed the way that tools could help organize and help developers understand large codebases.
</li>
<li><a href="http://office.microsoft.com">Microsoft Office</a><br>
When our team members worked on the Microsoft Office codebase in 2000, we felt the need for and built our first automated tools for generating such class hierarchy diagrams.</li>
<li><a href="http://www.csail.mit.edu/">MIT CSAIL</a> - <a href="http://relo.csail.mit.edu">Relo Project</a><br>
The Relo project at CSAIL was created in 2003 by our team members to explore the evolution of such tools to have developers easily create class hierarchy diagrams. </li>
<li><a href="http://www.accenture.com/Global/Services/Accenture_Technology_Labs/default.htm">Accenture Research Labs</a><br>
The Relo project was then used by the team to help developers gain high-level views of code using Layered Diagrams. </li>
</ul>
Our experiences at Architexa in building the above tools have taught us the strengths and weaknesses of the different approaches. Since 2007 we have been working on an approach to integrate these ideas and re-implement the tools into a single Rich Source Exploration Tool Suite.<br><br>

<h2>People</h2>

A number of people have given us great patient feedback and have helped us with the product. We wanted to explicitly mention some of the major contributions:
<ul>
<li>Daniel Tunkelang: The initial graph layout used by the Relo project was based on Daniel's PhD thesis. Daniel's thesis looks into effective layout of complex graphs. In an effort to provide users with a simple experience, over time, the needs of our graph layout have changed significantly and we are now using a custom implementation for our diagrams.</li>
<li>Cyrus Kalbrener: Our tool suite has always had a strong focus on reducing the memory footprint, however in the process our tool's runtime performance was slow. Cyrus did some of the initial work at improving our builder's performance and by evaluating various bytecode extraction frameworks. His approach gave us most of the insights at performance tuning that we are still using.</li>
</ul>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
