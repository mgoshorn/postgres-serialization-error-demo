CREATE TABLE colors (
	id SERIAL PRIMARY KEY,
	color VARCHAR(5) NOT NULL
);

INSERT INTO colors
  WITH x(id) AS (SELECT generate_series(1,10))
  SELECT id, CASE WHEN id % 2 = 1 THEN 'black'
    ELSE 'white' END FROM x;
