
                                                     
              UNITED NATIONS ECONOMIC COMMISSION FOR EUROPE
                             Trade Division
             Palais des Nations, CH-1211, Geneva, Switzerland
    Tel:+41 22/917 2773   Tlx: 412962 UNO CH   Fax:+41 22/917 0037


           


			   UN/EDIFACT



			D.96B DIRECTORY 

		

                           1996-07-08


SOURCE:	UN/EDIFACT RAPPORTEURS
STATUS:	DRAFT
ACTION:	FOR ACCEPTANCE BY WP.4 AT THE SEPTEMBER 1996 SESSION

The directory and other Trade Facilitation Information is also available 
on the UN/ECE WorldWideWeb site, page: "www.unicc.org/unece/trafix"
	
------------------------------------------------------------------------

The interactive EDI (I-EDI) directories have been implemented in D.96B in 
accordance with the decision made at the March 1996 session of GE.1, 
and the subsequent clarification by the UN/EDIFACT Steering Group (ESG).

However, since a revision of TRADE/WP.4/R.1023 incorporating the changes 
necessary to support the interactive directories has not yet been approved,
the basic documentation remains unchanged.

The I-EDI directories have been labelled TIMD for the interactive message
directory, and the directories needed to support the interactive messages
have been labelled: 
   TISD for the interactive segment directory,
   TICD for the interactive composite directory and
   TIED for the interactive data element directory.
They respect the layout of R.1023 with the exception of the TISD which has
been slightly changed in order to incorporate the identification of the use
of the repeating factor for composite data elements and stand alone data 
elements in the segment directory (see temporary layout below).

The code list directory is common to both the interactive directory and the
batch directories

It must be noted that the syntax upon which all of the I-EDI directories
and associated messages are based is that published in document 
TRADE/WP.4/R.1157/Add.2 on 28 July 1995.


USAGE OF THE DISKETTE

- The file AUDIT-D.96B contains the Directory Audit Team Statement for this
directory. 

- The file CONTENTD.96B (contained in document.zip) is the table of contents
   of the UN/EDIFACT Draft Directory.


- The files DOCUMENT.ZIP and D96B.ZIP are compressed 
versions in ASCII (code page 437 United States) of the UN Trade Data
Interchange Directory using the compression program PKZIP. The contents of
Directory files is described in the file CONTENTD.96B. 

To get the decompressed version of the Directory, use the program PKUNZIP.EXE 
stored on the diskette.

Type the commands:

 PKUNZIP [x:]DOCUMENT.ZIP  [y:][path]
 PKUNZIP [x:]D96B.ZIP      [y:][path]

  
 where
  x:      is the drive on which the diskette is stored
  y:      is the drive on which the decompressed version will be stored.
  path    specifies the path to the directory where the decompressed
          version will be stored.


TRADE/WP.4/R.1023 TEMPORARY LAYOUT

5.2 Segment directory specifications

The segment specification for each segment shall be defined in the following
manner:

column:
1  5  6  7   14         25                              58 60  63    70

2.   Segment specifications

     Change indicators

     a plus sign (+)    for an addition
     an asterisk (*)    for an amendment to structure
     a hash sign (#)    for changes to names
     a vertical bar (|) for changes to text for descriptions,
                        notes and functions
     a minus sign (-)   for a deletion
     a X sign (X)       for marked for deletion

----------------------------------------------------------------------

    | AGR    AGREEMENT IDENTIFICATION

      Function: To specify the agreement details.   col:58 

010   C543   AGREEMENT TYPE IDENTIFICATION               9 C  
      7431   Agreement type qualifier                      M  an..3
      7433   Agreement type, coded                         C  an..3
      1131   Code list qualifier                           C  an..3
      3055   Code list responsible agency, coded           C  an..3
      7434   Agreement type description                  9 C  an..70

The change indicator is placed in column 5 if there is only one. 
For two change indicators columns 4 and 5 shall be used. The change 
indicators may appear against the segment tag, the function, 
any eventual note, and the composite or elementary data elements.
(Note : these indications, with the exception of the indication
 "marked for deletion", are kept local to the directory and do not
permeate throughout the directories).

The specific meaning of the change indicators within the segment 
directory is as follows :

"+" :	This signifies that a segment has been added to the directory 
        and is indicated immediately before the segment tag. 
        It can also be used to indicate the addition of an simple data 
        element or a composite data element to an existing segment 
        in which case it is indicated immediately before the data element 
        tag which has been added.
"*" :	This sign is used to indicate that a data element (simple or 
        composite) has been added to or suppressed from the segment. 
        It is also used if a data element within the segment has been 
        marked for deletion or if the status of a data element within 
        the segment has changed. The sign is marked immediately before 
        the segment tag and immediately before the simple or composite 
        data elements if there is a change of status.
"|" :	This sign is used to indicate changes in the functional 
        description and note sections of a segment. If such a case occurs 
        then the vertical bar is marked immediately before the segment 
        tag as well as immediately before the section modified.
"#" :	The hash sign is used to indicate a change in the name of the 
        segment. If such a case occurs then the hash sign is marked 
        immediately before the segment tag.
"X" :	The X sign is used to indicate segments which have been marked 
        for deletion after an interval of three years. At the end of 
        the three year period, the segments so marked will be automatically 
        deleted from the directories. (The X sign should be permeated 
        throughout the different directories to ensure that all effected 
        information is identified) The X is marked immediately before 
        the segment tag.

	If a data element has been marked for deletion in the ED or CD 
        an X sign shall be marked before the data element tag in the segment.

The position indicator in column 1 starts at 10 and is incremented by 10 
for every composite and simple data element in the segment.

The repetition count shall begin in column 58 and shall be right justified from
that column (i.e. a two digit number begins in column 57).

The repetition count shall only be shown against a composite data element or a 
stand-alone data element. It shall not be shown against component data elements.

The status shall be "M" for mandatory components of the segment and "C" for 
conditional components of the segment. The component data elements within a 
composite must indicate the same status as found in the composite directory.

The format representation shall conform to the specification in ISO 9735.

The segment, data element and composite data element names shall use only 
capital letters. Component data element names shall only have the first 
letter of the name in capitals.

There should be one blank line between each data element and composite 
data element, but no blank lines between component data elements or between 
a component data element and the composite data element to which it belongs.

If the functional description exceeds 53 characters then it should be 
continued on the following line in column 17.

In a similar manner if a note exceeds 53 characters then it should be 
continued on the following line in column 17.

If the names of the data elements in the segment exceed 40 characters 
they shall be continued on the following line in column 14.

-------------------------------------------------------------------
   PKUNZIP is a product of PKware:
      
      PKware, Inc.
      7545 North Port Washington Road
      Glendale, WI 53217-3422
      Fax: 414 352 3815

 PKUNZIP(tm) FAST! Extract Utility
 Copyright 1989 PKWARE Inc. All Rights Reserved

PKWARE, INC. HAS DONATED THE USE OF THE PKUNZIP(tm) DATA EXTRACTION
         UTILITY TO UN/ECE FOR USE WITH UNTDED AND UNTDID.

------------------------------------------------------------------
