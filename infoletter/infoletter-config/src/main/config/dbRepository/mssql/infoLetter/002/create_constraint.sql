ALTER TABLE SC_IL_Letter WITH NOCHECK ADD 
	 CONSTRAINT PK_InfoLetter_Letter PRIMARY KEY  CLUSTERED 
	(
		id
	)   
;

ALTER TABLE SC_IL_Publication WITH NOCHECK ADD 
	 CONSTRAINT PK_InfoLetter_Publication PRIMARY KEY  CLUSTERED 
	(
		id
	)   
;