
alter table sc_delegatednews_news with nocheck 
        add constraint pk_delegatednews_news
        primary key clustered (pubId);

alter table sc_delegatednews_news
        add constraint fk_delegatednews_news_pubid
        foreign key (pubId)
        references SB_Publication_Publi (pubId);
