/*Examen No. 3
 Motor: MySQL
 Autor: Andres Arroyave Londoño
 ID SIGAA: 000547528
 */

-- Dragones (5)
INSERT INTO dragon (id, nombre, color, tamano) VALUES
('6660c197-db44-4a88-aba2-7a09d7f82938', 'Caraxes', 'rojo', 'grande'),
('ce5b5cee-f235-436b-bade-3206e415f3be', 'Meleys', 'rojo', 'grande'),
('cd9e7f23-4159-4b6f-9922-f5e4e84971e5', 'Syrax', 'amarillo', 'mediano'),
('991920d7-7f11-4d71-a470-65c7f27428e3', 'Vermax', 'gris', 'pequeno'),
('c01ce52a-6d84-4aea-a4d0-091eb54dbb7a', 'Vhagar', 'bronce', 'colosal');

-- Jinetes (6)
INSERT INTO jinete (id, nombre, casa) VALUES
('35d740c4-9a07-4ae7-8c4e-817c4bf15cca', 'Aemond Targaryen', 'Targaryen'),
('02ec845d-13af-4e14-8240-55e17b6369ff', 'Daemon Targaryen', 'Targaryen'),
('032e8759-96f7-41cb-b4e5-8a3341f20b49', 'Jacaerys Velaryon', 'Velaryon'),
('ea86cf26-2679-459a-9317-eb830521ffd8', 'Laena Velaryon', 'Velaryon'),
('8b53afae-8dac-4e46-bf04-7b7fb32b21bf', 'Rhaenyra Targaryen', 'Targaryen'),
('1f5dd74a-7eb9-4779-9483-a1d590a0ad8c', 'Rhaenys Targaryen', 'Targaryen');

-- Guaridas (4)
INSERT INTO guarida (id, nombre, ubicacion) VALUES
('fb35ce7a-ccf2-4254-a59a-00686d316011', 'Fosa del Dragón', 'Desembarco del Rey, Tierras de la Corona'),
('71df5ea7-7672-49f1-a5df-596431dd788f', 'Harrenhal', 'Tierras de los Ríos, Poniente'),
('b663cea5-d855-46e5-be23-bb0f1ad34eb5', 'Marcaderiva', 'Isla de Marcaderiva, Mar Angosto'),
('e9496486-13f3-4c4e-a372-be115103649f', 'Rocadragón', 'Isla de Rocadragón, Mar Angosto');

-- jinete_dragon (7 asociaciones)
-- - Vhagar con 2 jinetes: Aemond y Laena
-- - Rhaenyra con 2 dragones: Syrax y Meleys
INSERT INTO jinete_dragon (id, jinete_id, dragon_id) VALUES
('4e9cd08d-4d9d-4fee-87e3-6e51194549ee', '35d740c4-9a07-4ae7-8c4e-817c4bf15cca', 'c01ce52a-6d84-4aea-a4d0-091eb54dbb7a'),
('87b31517-6e3a-4e54-a7d2-35ae05652bae', '032e8759-96f7-41cb-b4e5-8a3341f20b49', '991920d7-7f11-4d71-a470-65c7f27428e3'),
('98278740-36d2-40c8-854c-a12452a17eb8', '8b53afae-8dac-4e46-bf04-7b7fb32b21bf', 'cd9e7f23-4159-4b6f-9922-f5e4e84971e5'),
('c0d2ae4a-9947-411b-adc7-8f2c147c8542', '1f5dd74a-7eb9-4779-9483-a1d590a0ad8c', 'ce5b5cee-f235-436b-bade-3206e415f3be'),
('c10b66fd-4e2f-41b0-8c23-6098c0d41d76', 'ea86cf26-2679-459a-9317-eb830521ffd8', 'c01ce52a-6d84-4aea-a4d0-091eb54dbb7a'),
('dcfdeabe-4fcd-4f49-b3c8-0106c9bad9d5', '8b53afae-8dac-4e46-bf04-7b7fb32b21bf', 'ce5b5cee-f235-436b-bade-3206e415f3be'),
('e91b8c1f-17a7-4f51-9a58-c5a6fca9fe44', '02ec845d-13af-4e14-8240-55e17b6369ff', '6660c197-db44-4a88-aba2-7a09d7f82938');

-- dragon_guarida (9 asociaciones)
-- - Dragón con 2 o más guaridas: Caraxes, Syrax, Vhagar, Meleys
-- - Guarida con 2 o más dragones: Rocadragón, Fosa del Dragón, Harrenhal, Marcaderiva
INSERT INTO dragon_guarida (id, dragon_id, guarida_id) VALUES
('1b0d8e4d-0c7d-4ee1-8f66-4deb2ef99ca2', '991920d7-7f11-4d71-a470-65c7f27428e3', 'e9496486-13f3-4c4e-a372-be115103649f'),
('3a4a4cbc-7fc6-4625-a316-0d84200bc0bb', '6660c197-db44-4a88-aba2-7a09d7f82938', 'e9496486-13f3-4c4e-a372-be115103649f'),
('535cfb38-0c33-479e-97e5-e69dc6211de5', 'ce5b5cee-f235-436b-bade-3206e415f3be', 'fb35ce7a-ccf2-4254-a59a-00686d316011'),
('6d683269-0437-4f60-ab98-ca69fd28853e', '6660c197-db44-4a88-aba2-7a09d7f82938', '71df5ea7-7672-49f1-a5df-596431dd788f'),
('b316a1ce-5f92-4e28-836f-e5b36735eb52', 'c01ce52a-6d84-4aea-a4d0-091eb54dbb7a', '71df5ea7-7672-49f1-a5df-596431dd788f'),
('cbf471fb-f1a1-4b67-865f-6e02f4bdb268', 'cd9e7f23-4159-4b6f-9922-f5e4e84971e5', 'e9496486-13f3-4c4e-a372-be115103649f'),
('d0f0d4ed-7dde-4cdd-bc61-d0e3b896e9a7', 'cd9e7f23-4159-4b6f-9922-f5e4e84971e5', 'fb35ce7a-ccf2-4254-a59a-00686d316011'),
('d893fb67-e06f-4569-87d9-1d7504388e6a', 'c01ce52a-6d84-4aea-a4d0-091eb54dbb7a', 'b663cea5-d855-46e5-be23-bb0f1ad34eb5'),
('f2dbe52e-96fb-4614-b07f-f44e80c36de8', 'ce5b5cee-f235-436b-bade-3206e415f3be', 'b663cea5-d855-46e5-be23-bb0f1ad34eb5');
