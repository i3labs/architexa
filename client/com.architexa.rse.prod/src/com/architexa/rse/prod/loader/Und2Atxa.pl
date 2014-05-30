
use Understand;
use Getopt::Long;
use strict;

my $dbPath;
GetOptions(
        	"db=s" => \$dbPath,
        );

die usage("-db argument is required\n\n") unless ($dbPath);

my $db = openDatabase($dbPath);

# get all files
# for each file go into all contained content and dump
#
# type 0: number of files
# N Cnt
#
# type 1: dump relationships of the form 'R AUID1 "rel kind" AUID2'
# where R is a control (and there will be more)
# AUID is of the form PATH.TO.DIRECTOR$Nested$Structure.member$$$$
# $$$$ would be () for method
#
# type 2: entity information of the form TBD later
#
#
# For implementation number of files = classes + interfaces 
# (understand does not give files -> classes relationship only 
# classes -> files)

# api: http://www.scitools.com/documents/manuals/pdf/understand_api.pdf
# possible http://blog.zerodogg.org/2010/03/02/a-very-simple-one-liner-repl-for-perl/repl
# to run: C:\Dev\runtime-empty-space>"c:\Program Files\SciTools\bin\pc-win64\uperl.exe" u2a.pl
# to debug: C:\Dev\runtime-empty-space>"c:\Program Files\SciTools\bin\pc-win64\uperl.exe" -d baseScript.pl
my @ents = sort {$b->longname() <=> $a->longname();} ($db->ents("class"), $db->ents("interface"));
#dmpEnt(@ents[2]);
dmpEnts(\@ents);  # TBD next here --- we need to get better at diving in (and lots of testing to be done)
closeDatabase($db);



# model
sub dropCtxt($) {
    my ($name) = @_;
	$name =~ s/.*\.//;
	return $name;
}
sub getAUID($) {
    my ($ent) = @_;
	#return $ent->longname();

	# we deal with three kinds here - 
	# i. folders/packages
	# ii. classes, interfaces, structures, enums
	# iii. methods, fields, variables
	# ....and stuff that we need to ignore
	if (
			$ent->kind()->check("package")
		) {
			# these should not be getting called anymore
   			return $ent->name();
	} elsif (
			$ent->kind()->check("file")
		) {
			# we have already gotten type from child - so we just need the
			# folder name here (and folder name is stored with '.' and without
			# special characters
			my $myName = $ent->longname();
			$myName =~ s/\\[^\\]*$//;	# drop file name
			$myName =~ s/[\\\/\{:#]/./g;	# change special characters to '.'
			$myName =~ s/\.\././g;		# remove multiple '.'
			#return ">" . $ent->longname() . "<";
			return $myName;
	} elsif (
			$ent->kind()->check("class") ||
			$ent->kind()->check("interface") ||
			$ent->kind()->check("enum")
		) {
   			return getAUID(parent($ent)) . "\$" . dropCtxt($ent->name());
	} elsif (
			$ent->kind()->check("method")
		) {
   			return getAUID(parent($ent)) . "." . dropCtxt($ent->name()) . "()";
	} elsif (
			$ent->kind()->check("variable")
		) {
   			return getAUID(parent($ent)) . "." . dropCtxt($ent->name());
	} elsif (
			$ent->kind()->check("unknown") ||
			$ent->kind()->check("unresolved") ||
			$ent->kind()->check("unused")
		) {
			# we don't care about these
	} elsif (
			$ent->kind()->check("parameter")
		) {
			# we likely need to figure stuff out here
	} else {
		print "Cannot get id [unsupport kind]: ", 
				$ent->kind()->longname(), 
				" {", $ent->longname(), "}",
				"\n";
	}
	return "?".$ent->name();
}

# graph traversal
sub parent($) {
    my ($ent) = @_;

    my @rels = $ent->refs;
	foreach my $rel (@rels) {
		if ($rel->kind()->check("definein")) {
			return $rel->ent();
		} else {
		}
	}
	#print "Error - Entity has no parent: ", $ent->longname();
}

# actual exporting methods
sub dmpRels($) {
    my @rels = @{$_[0]};
	my @children = ();
	foreach my $rel (@rels) {
		if (!isFwdRel($rel)) {
			# we always skip these
		} elsif ($rel->kind()->check("define")) {
			push(@children,$rel->ent());
		} elsif ($rel->kind()->check("definein")) {
		} elsif ($rel->kind()->check("end")) {
		} else {
			dmpRel($rel);
		}
	}
	return (\@children);
}
sub isFwdRel($) {
    my ($rel) = @_;
	my $relName = $rel->kind->longname();
	my $revName = $rel->kind->inv()->longname();
	if (length($relName) < length($revName)) {
		return 1;
	} else {
		return 0;
	}
}

sub dmpRel($) {
    my ($rel) = @_;
   	print "R,",
		getAUID($rel->scope()), ",", 
		"\"", $rel->kind->longname(), "\",", 
		getAUID($rel->ent()), "\n";

}

sub dmpEnt($) {
    my ($ent) = @_;
   	print "E,",
		getAUID($ent), ",", 
		"\"", $ent->kindname(), "\",";
	if ($ent->kind()->check("variable")) {
		print $ent->type(), "," 
	}
	print
		#$ent->uniquename(),
		"\n";
	my @r = $ent->refs;
	my @children = @{ dmpRels(\@r) };
	#my $childCnt = @children;
	#print "  child-cnt: ", $childCnt, "\n";
	dmpEnts(\@children);
}

sub dmpEnts($) {
    my @ents = @{$_[0]};
	my $entsCnt = @ents;
	print "C,", $entsCnt, "\n";
	#my $cnt = 1;   	
	foreach my $ent (@ents) {
		#print "I,", $cnt, "\n";
		#$cnt = $cnt + 1;
		dmpEnt($ent);
	}
}

sub openDatabase($) {
    my ($dbPath) = @_;
    
    my $db = Understand::Gui::db();

    # path not allowed if opened by understand
    if ($db&&$dbPath) {
        die "database already opened by GUI, don't use -db option\n";
    }

    # open database if not already open
    if (!$db) {
        my $status;
        die usage("Error, database not specified\n\n") unless ($dbPath);
        ($db,$status)=Understand::open($dbPath);
        die "Error opening database: ",$status,"\n" if $status;
    }
    return($db);
}

sub closeDatabase($) {
    my ($db)=@_;

    # close database only if we opened it
    $db->close() if ($dbPath);
}

sub usage ($) {
    return << "END_USAGE";
${ \(shift @_) }
Usage: xxxx
END_USAGE
}

