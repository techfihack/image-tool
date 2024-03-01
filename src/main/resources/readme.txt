
- Things in use : Java spring boot, MySql, Redis, HTML and JS.

Made Assumptions
- point system
- image type format (jpg), different image format different writer, might have issues.
- image storage currently not in use,   for next enhancement
- database design
- UI layout and bugs

Validation Point System for Multi Tiling ( Customize-able)
- max point = number of correct labelling image in database
  ex : a 9 tiles picture have 4 tiles having 'blue' label => maximum marks : 4 marks

- user points and toleration area
  1 correct image selected by user, will score 1 point
  1 incorrect image selected by user, will deduct 1 point
  toleration area is the answer image max point +- 1 point
  ex : 4 marks should be the maximum marks, if the user only send 3 picture, and 3 of them also correct, means +3 points.
  and toleration of this should be '3,4,5' since 4 is the maximum point
  so user score 3 points, and validation is passed

  ex: if the user only send 5 picture, and 3 of them also correct, 2 of them incorrect, means +3 - 2 = 1 mark
  so user score 1 point, and validation is failed






