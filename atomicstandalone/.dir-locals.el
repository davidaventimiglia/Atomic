;;; Directory Local Variables
;;; See Info node `(emacs) Directory Variables' for more information.

((java-mode
  (compile-command . (concat "cd " (let ((l (dir-locals-find-file (or (buffer-file-name) default-directory)))) (if (listp l) (car l) l)) " && mvn -B compile"))))
