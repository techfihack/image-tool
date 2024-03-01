
use pip;

select label_name, labels.id as label_id, image_labels.image_id from image_labels
inner join labels
on image_labels.label_id = labels.id
where labels.label_name = 'happy';