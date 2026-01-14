-- Migration pour mettre à jour la liste des classes thérapeutiques autorisées
ALTER TABLE medications DROP CONSTRAINT IF EXISTS medications_therapeutic_class_check;

ALTER TABLE medications ADD CONSTRAINT medications_therapeutic_class_check 
CHECK (therapeutic_class IN (
    'ANTALGIQUE', 
    'ANTIBIOTIQUE', 
    'ANTIPALUDEEN', 
    'ANTIHYPERTENSEUR', 
    'ANTIINFLAMMATOIRE', 
    'ANTIDIABETIQUE', 
    'ANTIHISTAMINIQUE', 
    'ANTIPYRETIQUE', 
    'VITAMINE', 
    'AUTRE'
));
