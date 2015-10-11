<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Code Relationships via Class Diagrams | Architexa Labs</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<h1>Labs - Code Understanding via Exploratory Tools</h1>

<div style="text-align:justify">
<div class="note">
Note: This document represents the main ideas of work done earlier in our labs. The tool has since had major updates and is currently in beta. Contact us at <a href="mailto:info@architexa.com">info@architexa.com</a> for more information.
</div>

<div style="float:right;padding:15px">
<b>By:</b><br>
Vineet Sinha, Architexa<br>
Rob Miller, MIT CSAIL<br>
David Karger, MIT CSAIL
</div>


<h2>Code Relationships via Class Diagrams</h2>


<p><b>Summary:</b> As software systems grow in size and use more third-party 
libraries and frameworks, the need for developers to understand unfamiliar 
large codebases is rapidly increasing. We present a tool, Relo, that supports 
users’ understanding by allowing interactive exploration of code. As the 
developer explores relationships found in the code, Relo builds and 
automatically manages a visualization, similar to a UML Class Diagram, 
mirroring the developer’s mental model, allowing them to group viewed artifacts 
or use the viewed items to ask the system for further exploration 
suggestions.</p>


<p><b>Publication:</b> &ldquo;Incremental exploratory visualization of relationships in large codebases for program comprehension&rdquo;. In <i>the 20th Annual ACM SIGPLAN Conference on Object-Oriented Programming, Systems, Languages, and Applications</i> OOPSLA '05. ACM. [<a href="http://doi.acm.org/10.1145/1094855.1094933">link</a>]</p>

<p><b>Keywords: </b> Program Comprehension, Software Visualization, Large Software Systems.</p>

<h3>Introduction</h3>

<p>Working with the complexity of large software projects is a
pervasive and growing problem. Developers face increasing
difficulties in comprehending and maintaining a mental model of
the code in large codebases. While techniques like object-oriented
programming and design patterns have helped control complexity
by allowing developers to create and use appropriate abstractions
and encapsulate inessential details, these techniques require a
developer reading the code to follow many kinds of relationships
at once. For example, following a function call, once a simple
task, now also requires keeping track of inheritance and
polymorphism.</p>



<p>Relo is a program comprehension tool which
helps developers to understand the roles of the multiple types of
relationships in a software system. Relo visualizations start with a
single code artifact (such as a package, class, or method), from
which a user can browse the different types of relationships to
incrementally add more code artifacts. Relo helps maintain
context while users manage the visualization by choosing to
remove or group artifacts together. Such visualizations, like
concern graphs [<a href="http://doi.acm.org/10.1145/581339.581390">ref</a>],
 represent only a small manageable part of the
code and do not include irrelevant details allowing a user to focus
on the important relationships.</p>


<p>Relo visualizations try to be intuitive to end-users, showing code
artifacts in diagrams similar to UML Class Diagrams, while at the
same time allowing developers to zoom in to view and edit code
using text editors embedded in the graph. Developers can
therefore abstract to a high level, or zoom-in to see code. Relo
further helps maintain users’ focus by providing explicit support
for exploration while managing the amount and presentation of
information to the user based on his/her interaction with code
elements.</p>


<h3>Walkthrough</h3>

<!--
<div class="floatImage">
<img src="/technology/labs/images/relo-image001.jpg"><br>
Figure 1: Relo started<br> by opening EllipseFigure<br><br>
<img src="/technology/labs/images/relo-image002.jpg"><br>
Figure 2: After adding the<br> method basicMoveBy<br><br>
<img src="/technology/labs/images/relo-image003.jpg"><br>
Figure 3: Clicking on the class<br> to show the handles on it<br><br>
<img src="/technology/labs/images/relo-image004.jpg"><br>
Figure 4: Clicking on the<br> inheritance handles
</div>
-->
<div style="font-size:80%">
<table width="100%"><tr>
<td align="center" bgcolor="white">
<img src="/technology/labs/images/relo-image001.jpg">
</td><td align="center" bgcolor="white">
<img src="/technology/labs/images/relo-image002.jpg">
</td><td align="center" bgcolor="white">
<img src="/technology/labs/images/relo-image003.jpg">
</td>
</tr><tr>
<td align="center">
Figure 1: Relo started<br> by opening EllipseFigure<br><br>
</td><td align="center">
Figure 2: After adding the<br> method basicMoveBy<br><br>
</td><td align="center">
Figure 3: Clicking on the class<br> to show the handles on it<br><br>
</td></tr></table>
</div>

<div class="floatImage">
<img src="/technology/labs/images/relo-image004.jpg"><br>
Figure 4: Clicking on the<br> inheritance handles
</div>

<p>Consider the scenario (as mentioned in [<a href="http://doi.acm.org/10.1145/643603.643622">ref</a>]) of a user trying to
understand a portion of the JHotDraw project (<a href="http://www.jhotdraw.org/">link</a>), which lets users
draw a variety of figures including lines, triangles, rectangles, etc.
Let us trace through the steps of a developer trying to understand
how to operate on figures. The developer starts by examining a
package called figure that seems related to the task and is one of
the small number of packages in the project. The developer
continues by opening the class EllipseFigure in Relo.
</p>


<p>
On starting Relo with EllipseFigure the user is presented with
Figure 1. The figure shows that the class has 15 members, and the
user clicks on the menu to see a list. Considering the method
‘basicMoveBy’ as potentially interesting, he clicks on the method
name in the menu and thereby adds the method to the diagram
(Figure 2). Once added the user clicks on the class and is
presented with a vertical handle indicating the class inherits from
another class (shown in Figure 3). At a glance the user knows that
the no other classes inherit from it, and notices that he is provided
with handles for expanding, shrinking, or removing the class. The
user continues his exploration by clicking upwards through the
superclasses (Figure 4).
</p>

<div class="floatImage">
<img src="/technology/labs/images/relo-image005.jpg"><br>
Figure 5: Expanding the class AbstractFigure and the<br>
method addFigureChangeListener
</div>
<div class="floatImage">
<img src="/technology/labs/images/relo-image006.jpg"><br>
Figure 6: Asking for callers of addFigureChangeListener
</div>

<p>
At this point in time the user has an idea of the inheritance tree of
figures in the project, and he can choose to expand the topmost
class with an implementation. After double-clicking to see all
public methods and remove irrelevant methods, the user decided
to expand the addFigureChangeListener method, as it seems
to be part of the general framework for changing figures.
</p>


<p>
The user is presented with the Figure 5, which shows the
implementation of the method. A simple option will be to collapse
the class AbstractFigure and to examine a caller of
addFigureChangeListener. On clicking the caller handle on
the base class, Relo continues to build the graph (shown in Figure
6), and has begun to act as both a call-hierarchy browser and an
inheritance-hierarchy browser. Unlike previous graphical or
tree widget based exploratory approaches used in most IDE’s, all the relationships
are being shown by Relo in one place instead of having separate
views for each relationship. Additionally, the diagram only shows
the queried-for code elements and does not need to make visible
all the other classes in the shown packages, as would be done by
tools only supporting expansion via visual containment [<a href="http://portal.acm.org/citation.cfm?id=853250&dl=GUIDE&coll=GUIDE&CFID=29016428&CFTOKEN=20786494">ref</a>].
Finally, the diagram shows some unexpected relationships, such
as some calling methods belong to the same class, an observation
that would not have been possible by showing the relationships
and code elements in a single tree widget as done by Janzen et al. [<a href="http://doi.acm.org/10.1145/643603.643622">ref</a>].
</p>


<p>
The user can further continue using Relo, as the tool goes on to
build a larger visualization in order to supplement the user’s
understanding of the codebase.
</p>

</div>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
