select coach_id, tournament_name ,tournament_id, season_id, season, count(1) as num,
sum(case when wdl = 0 then 1 else 0 end) as loss,
sum(case when wdl = 1 then 1 else 0 end) as draw,
sum(case when wdl = 3 then 1 else 0 end) as win
from (
select a.coach_id,b.team_id, a.home_team_id, a.away_team_id,a.tournament_id, a.tournament_name, a.season_id,a.season, a.result,
case
when b.team_id = a.home_team_id then (case
WHEN CONVERT(SUBSTRING(a.result, 1, LOCATE(':',a.result)-1), SIGNED) > CONVERT(SUBSTRING(a.result, LOCATE(':',a.result)+1, CHAR_LENGTH(a.result)), SIGNED) THEN 3
WHEN CONVERT(SUBSTRING(a.result, 1, LOCATE(':',a.result)-1), SIGNED) < CONVERT(SUBSTRING(a.result, LOCATE(':',a.result)+1, CHAR_LENGTH(a.result)), SIGNED) THEN 0
else 1 end )
when b.team_id = a.away_team_id then (case
WHEN CONVERT(SUBSTRING(a.result, 1, LOCATE(':',a.result)-1), SIGNED) > CONVERT(SUBSTRING(a.result, LOCATE(':',a.result)+1, CHAR_LENGTH(a.result)), SIGNED) THEN 0
WHEN CONVERT(SUBSTRING(a.result, 1, LOCATE(':',a.result)-1), SIGNED) < CONVERT(SUBSTRING(a.result, LOCATE(':',a.result)+1, CHAR_LENGTH(a.result)), SIGNED) THEN 3
else 1 end ) end as wdl
from p_coach_match_detail as a
left join p_coach_career b on a.match_date > b.appoint_time and a.match_date < b.until_time and a.coach_id = b.coach_id and b.function = 'Manager'
) a 
where season_id >= 2017 and coach_id = 5075 group by coach_id, tournament_name ,tournament_id, season_id, season ORDER BY season_id DESC;
