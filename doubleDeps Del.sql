DROP VIEW IF EXISTS v_deps;

-- заполнение parentname 3 уровня ('parent name/parent-parentname/parent-parent-parent name')
create view v_deps as select *, 
	(SELECT CONCAT(t1.name,'/',ifnull(t2.name,''),'/',ifnull(t3.name,'')) 
		FROM deps AS t1 LEFT JOIN deps AS t2 ON t2.id=t1.parentid LEFT JOIN deps AS t3 ON t3.id=t2.parentid 
		where t1.id=a.PARENTID) par3 
	from deps a where a.parentname is null
;

update deps a, v_deps set a.parentname = v_deps.par3 WHERE a.id = v_deps.ID;
DROP VIEW `v_deps`;
update deps a set a.parentname= '--' WHERE a.parentname is null;


-- снятие метки об удалении у дубликатов с меньшим ID
create view v_deps as 
	SELECT  max(b.id) max_id  
    FROM deps b  
    -- where count(b.name) > 1 
    GROUP BY b.name, b.parentname
;
update deps set deleted=false where id not in (select max_id from v_deps);
DROP VIEW `v_deps`;

-- перед удалением дубликатов с большим ID, проверить есть ли их ID где-то в PARENTID
-- и заменить на id сохраненной копии ( а так же parent на idr сохраненной копии
UPDATE deps v set v.parentid= 
	(CASE WHEN 
		(select z.id FROM 
			(SELECT * from deps where id in (SELECT min(b.id) FROM deps b GROUP BY b.name, b.parentNAME)) as a, 
            (SELECT * from deps where id not in (SELECT max(b.id) FROM deps b GROUP BY b.name, b.parentNAME)) as z, 
            (SELECT * from deps where id not in (SELECT min(b.id) FROM deps b GROUP BY b.name, b.parentNAME)) as x 
        WHERE a.parentid=x.id and x.name=z.name and x.parentname=z.parentname and V.ID=a.ID) is null
    THEN v.parentid 
    ELSE  (select z.id FROM
            (SELECT * from deps where id in (SELECT min(b.id) FROM deps b GROUP BY b.name, b.parentNAME)) as a, 
            (SELECT * from deps where id not in (SELECT max(b.id) FROM deps b GROUP BY b.name, b.parentNAME)) as z,
            (SELECT * from deps where id not in (SELECT min(b.id) FROM deps b GROUP BY b.name, b.parentNAME)) as x  
        WHERE a.parentid=x.id and x.name=z.name and x.parentname=z.parentname and V.ID=a.ID) 
	END)
;

-- удаление дубликатов с большим ID
create view v_deps as SELECT min(b.id) min_id FROM deps b GROUP BY b.name, b.parentname;
delete from deps where id not in (SELECT min_id FROM v_deps);
DROP VIEW IF EXISTS `v_deps`;

-- несоответствие parent и parentname
create view v_deps as 
	SELECT b.id as b_id, b.idr as b_idr, a.id a_id, a.idr, a.PARENT a_parent, a.PARENTID a_parentid, b. DELETED b_del ,a.DELETED a_del, concat(b.name, replace(b.PARENTNAME,"/","")) as b_name, replace(a.PARENTNAME,"/","") as a_name
	FROM deps a left join deps b on a.parentid = b.id
	where a.parent != b.idr -- and concat(b.name, replace(b.PARENTNAME,"/","")) = replace(a.PARENTNAME,"/","")
-- order by INSDATE desc
;

update deps a, v_deps v set a.parent = v.b_idr 
where a.id = v.a_id
;

-- 
select * from deps where id not in (select max_id from v_deps);
