	create table sc_delegatednews_news (
        pubId		int		not null ,
        instanceId		varchar(50)     not null,
        status varchar(100) not null,
        contributorId varchar(50) not null,
        validatorId varchar(50) null,
        validationDate timestamp(0) null,
        beginDate timestamp(0) null,
        endDate timestamp(0) null
	);
    
	alter table sc_delegatednews_news 
        add constraint pk_delegatednews_news
        primary key (pubId);
