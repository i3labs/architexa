<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Behaviors in Complex Software via Sequence Diagrams | Architexa Labs</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>

<h1>Labs - Code Understanding via Exploratory Tools</h1>

<div style="text-align:justify">
<div class="note">
Note: This document represents the main ideas of work done earlier in our labs. The tool has since had major updates and is currently in beta. Contact us at <a href="mailto:info@architexa.com">info@architexa.com</a> for more information.
</div>

<div style="float:right;padding:15px">
<b>By:</b><br>
Elizabeth Murnane, Architexa<br>
Vineet Sinha, Architexa<br>
</div>

<h2>Behaviors in Complex Software via Sequence Diagrams</h2>

<p><b>Summary:</b> We present Chrono, a tool that creates sequence 
diagram based visualizations. Since the diagrams produced by traditional 
sequence diagramming tools become large and unmanageable when dealing with 
complex code bases, Chrono focuses on removing less relevant information, 
condensing diagram components, and allowing for interactive exploration.</p>

<p><b>Publication:</b> &ldquo;Interactive exploration of compacted visualizations for understanding behavior in complex software&rdquo;. In <i>the 23rd ACM SIGPLAN Conference on Object-Oriented Programming Systems Languages and Applications</i> OOPSLA '08. ACM.
[<a href="http://doi.acm.org/10.1145/1449814.1449833">link</a>]</p>

<p><b>Keywords:</b> Complex Software Systems; Diagram Compression; Exploration; Interaction; Reverse Engineering; Software Behavior; Software Visualization. </p>

<h3>Introduction</h3>

<p>
Sequence diagrams are used to show the behavior of a system and allow developers to investigate how a system is used. A sequence diagram can be used to explore the life cycle of an element or the course that a task takes to complete. Problems arise, however, when processes get complicated since the diagrams produced by traditional tools can quickly become unmanageable. The growing abundance of software systems with complex codebases demands that developers are able to understand such processes, so Chrono aims to satisfy this need by using the following information management strategies to provide easy analysis of a developer's logic and demonstrate overviews of behavior.
</p>

<p>
 First, Chrono strives to make understanding as easy as possible by limiting the amount of information initially displayed, by providing users the ability to easily compress or hide components in the diagram, and by using unobtrusive visual components whenever possible to convey information. In these ways Chrono emphasizes the key parts of a system and the elements most important and relevant to each user.
</p>

<p>
Also, since Chrono presents users with this minimal, manageable amount of information and compacts representations in order to not overwhelm, it also gives users the ability to easily interact with and manipulate a diagram. Users can expand, collapse, add, or delete components in order to show more or less information, which allows for the incremental exploration of a large or complicated project.
</p>


<h3>Walkthrough</h3>

<!--
<div class="floatImage">
<img src="/technology/labs/images/strata-image001.jpg"><br>
Figure 1 - Top level view when a developer opens Strata<br>
on the entire JEdit codebase. (left: the default view, and<br>
right: when mouse is moved over the org pacakage)
</div>

<div class="floatImage">
<img src="/technology/labs/images/strata-image002.jpg"><br>
Figure 2 - Strata after the developer removes the modules other than org.
</div>
-->



<p>
Let us consider using Chrono in order to understand the source code of Lapis (<a href="http://groups.csail.mit.edu/uid/lapis/ ">link</a>), an open source web browser and text editor.
</p>

<div class="floatImage">
<img src="/technology/labs/images/chrono-image001.png"><br>
Figure 1: The representation<br>
 of a class in Chrono
</div>

<b>Exploration</b><br>

<p>
A user can begin exploring by opening a class in Chrono or by dragging it from the package explorer to the Chrono editor.
</p>

<p>
 When the class is selected, a button is shown that can be pushed to view the methods that class contains. When a method in the list is selected, it is added to the diagram, and the user can begin exploring how the system operates.
</p>
<div align="center">
<img src="/technology/labs/images/chrono-image002.png" width="362px"><br>
Figure 2: The list of methods declared inside the class<br><br>
</div>

<p>
Once a figure that represents a method declaration has been
added to the diagram, the user can select it and press a button
to see a list of all of the methods that are invoked in the declaration.
If no such button is displayed, the user immediately
knows that the method invokes nothing.
</p>
<div align="center">
<img src="/technology/labs/images/chrono-image003.png" width="362px"><br>
Figure 3: The list of invocations made by a selected method declaration
</div>


<p>
As can be seen in Figure 3, Chrono permits a user to only
add the most pertinent information to a diagram by showing all
of the methods that are invoked but only allowing application
calls to be displayed. (By application method we mean a method
defined in the developer’s code and not in an external library).
The user can choose to display all of the invoked application
methods, or he can select just one of them and gradually
progress through the life cycle of the method.
</p>

<p>
Similarly, a user can not only explore what calls a method makes by using the right side button, but he can also see what code invokes the selected method by pressing its left side button. By selecting one of the methods in this list, the invocation call is added to the diagram, and a user can trace back through the code's execution.
</p>



<b>Automatic compacting and filtering</b><br>

<p>
Because it is easy for sequence diagrams to get large fast, Chrono compacts and filters information by default and allows a user to expand, display, and explore components as desired in order to focus on the parts most relevant to him.
</p>


<p>
As seen in Figure 4, Chrono displays chained calls as a single message and allows a user to select one of the methods in the chain in order to expand and display it separately in the diagram.</p>

<div align="center">
<table><tr>
<td bgcolor="white" valign="top">
<img src="/technology/labs/images/chrono-image004.png" width="234"><br>
</td>
<td bgcolor="white">
<img src="/technology/labs/images/chrono-image005.png" width="345"><br>
</td>
</tr>
<tr>
<td colspan=2>
Figure 4: Chrono compacts chained calls and
 allows a user to expand them piece by piece
</td>
</tr></table>
</div>



<p>
	 Also, if a visualization contains instances that are ordered in such a way that an invocation message flows from right to left, Chrono assumes that this message is not the intention of the diagram since time increases from top to bottom and left to right in sequence diagrams, and it is hidden. The corresponding method invocation and declaration figures are highlighted in red, and only when the user hovers over one of them with the mouse is the backward call shown.
</p>

<p>
	 Finally, when the user scrolls below the bottom of an in-stance’s life line (the vertical line that descends from an in-stance and represents its life span), this instance is no longer participating in any of the processes that are in view. It is therefore currently extraneous, so Chrono hides it.
</p>

<b>Direct compacting and filtering</b><br>
<div class="floatImage">
<br><br><img src="/technology/labs/images/chrono-image006.png"><br>
Figure 5: Chrono only displays the if-then<br>
 statements and hides all others by<br>
 showing them as "..."
</div>
<p>
In addition to condensing and hiding components automatically, Chrono also allows a user to do this directly. To illustrate, we discuss first Chrono’s representation of control flow and then its filtering of non-application methods.
</p>

<p>
     In order to let a user visually observe the possible flow of control that a system could take depending on various conditions or tests, Chrono displays loops, iterations, and conditional statements. Of course, this could result in the same statement inside a loop being displayed multiple times or in a large number of paths being displayed when only one is actually executed. Chrono therefore eliminates an overabundance of information by displaying loop statements only once and by displaying only one of the possible execution paths and indicating that other paths exist but are not shown. For example, all possible branches in an if block are not displayed. Only the method calls that would execute if the initial if statement condition is true are shown, and the user can collapse or expand each portion of the block to reveal the statements in any else if and else blocks (initially shown as simply "..."). Chrono also indicates that method calls are part of a control flow block by surrounding them with a simple highlight that is only displayed when the mouse is hovered over the block.
</p>


<div align="center">
<table><tr>
<td bgcolor="white" valign="top">
<img src="/technology/labs/images/chrono-image007.png"><br>
</td>
<td bgcolor="white" valign="top">
<img src="/technology/labs/images/chrono-image008.png"><br>
</td>
</tr><tr>
<td colspan="2" align="center">
Figure 6: A user can collapse and expand the branches of a<br>
 control flow block, which is highlighted on hover
</td>
</tr></table>
</div>

<p>
Chrono’s interface also contains a button that when pressed hides all non-application classes and methods in the diagram. The user can reveal these hidden components by pressing the button again. This de-clutters the diagram and emphasizes the parts most relevant to the user - those corresponding to elements in his own code.
</p>

<p>
	 By continuing to use Chrono, the user can build a more comprehensive diagram in order to further model and make sense of how his system behaves.
</p>


</div>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
