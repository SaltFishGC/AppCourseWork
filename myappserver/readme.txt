给mysql数据库建了一个根据userid查询的索引：
CREATE INDEX idx_userid ON time_learned (user_id);
CREATE INDEX idx_userid ON netem_learned_detail (user_id);
CREATE INDEX idx_userid ON netem_full_list (id);
索引也不是越多越好，后续插入的数据项越多，索引越长，同样很占空间。