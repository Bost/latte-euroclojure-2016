
;;; # Live-coding Mathematics
;;; ## Your first Clojure proofs

;;  using the LaTTe proof assistant: <<<https://github.com/fredokun/LaTTe|||(lambda (x) (browse-url "https://github.com/fredokun/LaTTe"))>>>

         ;;;                  ((((
         ;;;                 ((((
         ;;;                  ))))
         ;;;               _ .---.
         ;;;              ( |`---'|
         ;;;               \|     |
         ;;;               : .___, :
         ;;;                `-----'  -Karl

;;; ### Frédéric Peschanski @ Euroclojure 2016

;;; Associate Professor at UPMC
;; (Université Pierre & Marie Curie, Paris - France)

;;; Researcher at LIP6
;; (Computer science laboratory)



;;; # To give credit where credit is due ...

;;; The theory underlying LaTTe (as well as its basic library)
;;; is heavily influenced by the following book:

;;; ## Type Theory and Formal Proof: an Introduction
;;; ### Rob Nederpelt and Herman Geuvers
;; Cambridge University Press, 2012

;;;               __...--~~~~~-._   _.-~~~~~--...__
;;;             //               `V'               \\ 
;;;            //                 |                 \\ 
;;;           //__...--~~~~~~-._  |  _.-~~~~~~--...__\\ 
;;;          //__.....----~~~~._\ | /_.~~~~----.....__\\
;;;         ====================\\|//====================
;;;                         dwb `---`  

;;; It is a wonderful book for (the few ?) people interested
;;; in such topics

;; (of course, you do *not* need to read the book to
;; use LaTTe, or hopefully to understand this talk!).




;;; # Let's get started ...

(ns latte-euroclojure-2016.full-long
  "This is a talk about LaTTe given @ Euroclojure 2016.
  (this is a preliminary version, too long but more
  complete of course)"

  ;; These belong to logic ;-)
  (:refer-clojure :exclude [and or not])

  ;; LaTTe core and main top-level forms
  (:require [latte.core :as latte
             :refer [definition defthm defaxiom defnotation
                     forall lambda ==>
                     assume have proof try-proof
                     term type-of check-type?]]

  ;; ... the "standard" library (propositions, quantifiers and equality) 
            [latte.prop :as p :refer [<=> and or not]]
            [latte.quant :as q :refer [exists]]
            [latte.equal :as eq :refer [equal]]))



;;; # You know `lambda`, right?

;;; Well, in Clojure, `lambda` is called `fn`  (Oh laziness!)
;;; ... we use it to construct first-class anonymous functions ...

;;; ### A trivial example: the identity function

((fn [x] x) 42)

;; what just happened is called a <<<beta-reduction|||t>>>
;; (a greek-ish translation for <<<function application|||t>>>)

;;; ### Another example: binary composition

((((fn [g] (fn [f] (fn [x] (f (g x)))))
   even?)                               ;; (==> int boolean)
   (fn [y] (if y "even" "odd")))        ;; (==> boolean String)
 42)



;;; # Lambda the ultimate

;;; ## Church thesis
;;; All computable functions can be encoded in the pure lambda-calculus
;;(only with single argument <<<fn|||t>>>'s, function application and variables)

;;; ### Example: the pairing function

(def pair (fn [x] (fn [y] (fn [z] ((z x) y)))))

;;; and accessors (or eliminators as you'll see)

(def fst (fn [p] (p (fn [x] (fn [y] x)))))
(def snd (fn [p] (p (fn [x] (fn [y] y)))))

;;; e.g.:

(fst ((pair "hello") 42))
(snd ((pair "hello") 42))



;;; # LaTTe (kernel) = Lambda with explicit types
;;; ## (a.k.a. a Type Theory)
;;;                _..._
;;;              .'     '.
;;;             /`\     /`\    |\         <<<but...|||(lambda (x) t)>>>
;;;            (__|     |__)|\  \\  /|
;;;            (     "     ) \\ || //
;;;             \         /   \\||//            <<<wait!?!|||(lambda (x) t)>>>
;;;              \   _   /  |\|`  /
;;;               '.___.'   \____/
;;;                (___)    (___)
;;;              /`     `\  / /
;;;             |         \/ /
;;;             | |     |\  /
;;;             | |     | "`
;;;             | |     |
;;;             | |     |
;;;             |_|_____|
;;;            (___)_____)
;;;            /    \   |
;;;           /   |\|   |
;;;          //||\\  Y  |
;;;         || || \\ |  |
;;;         |/ \\ |\||  |
;;;             \||__|__|
;;;              (___|___)
;;;         jgs  /   A   \
;;;             /   / \   \
;;;            \___/   \___/




;;; # Types, really?

;;; Yes! in LaTTe the 'lambda' abstractions are
;;; explicitely typed...

;;; ### Example: the (type-generic) identity function

(term (lambda [A :type]
        (lambda [x A] x)))

;; ^^^ look ma! a System F-ish term depending on a type! ^^^

;; e.g.:  (((lambda [A :type] (lambda [x A] x)) 42) int)
;;        --> ((lambda [x int] x) 42)
;;        --> 42



;;; # Let's do some type-checking ...

;;; ##What is the type of the (type-generic) identity?

(check-type?
 (lambda [A :type]
   (lambda [x A] x))

 ;; is of type ...

 (forall [A :type]
   (==> A A)))

;; or (forall [A :type]
;;      (forall [x A] A))



;;; # The type-generic composition function

(check-type?
 (lambda [A B C :type]
   (lambda [g (==> A B)]
     (lambda [f (==> B C)]
       (lambda [x A]
         (f (g x))))))

  ;; ... of type

 (forall [A B C :type]
   (==> (==> A B) (==> B C)
        (==> A C))))



;;; # The logical view...

;;; Given arbitrary types A, B and C

;;; ### - the type of the identity function on A is:

;;; (forall [A :type]
;;;   (==> A A))
;; "A implies A"  (reflexivity of implication)

;;; ###- the type of the composition function on A,B and C is:

;;; (forall [A B C :type]
;;;   (==> (==> A B)
;;;        (==> B C)
;;;        (==> A C)))
;; if "A implies B" and "B implies C" then "A implies C"
;; (transitivity of implication)

;;; ==> we've just experienced <<<Proposition-as-Type|||t>>> (PaT)
;;;     part of the <<<Curry-Howard correspondance|||t>>>



;;; # The type-generic pairing function

;; reminder
(def pair (fn [x] (fn [y] (fn [z] ((z x) y)))))

;; a type-generic version
(check-type? [A :type] [B :type] ;; <-- this is called the 'context'
   (lambda [x A]
     (lambda [y B]
       (lambda [C :type]
         (lambda [z (==> A B C)]
           ((z x) y)))))
   ;; of type...

   (==> A B
        (and A B)))

;; or  (==> A B
;;       (forall [C :type]
;;          (==> (==> A B C)
;;               C)))



;;; # Accessors = elimination rules

;; reminders
(def fst (fn [p] (p (fn [x] (fn [y] x)))))
(def snd (fn [p] (p (fn [x] (fn [y] y)))))

(check-type? [A :type] [B :type]
   (lambda [p (and A B)]
     ((p A) (lambda [x A] (lambda [y B] x))))
   ;; of type
   (==> (and A B)
        A))

;; --> this is <<<latte.prop/and-elim-left|||t>>>

(check-type? [A :type] [B :type] ;; <-- this is called 'the context'
   (lambda [p (and A B)]
     ((p B) (lambda [x A] (lambda [y B] y))))
   ;; of type
   (==> (and A B)
        B))

;; --> this is <<<latte.prop/and-elim-right|||t>>>



;;; # Deduction ...

;;; Logical (and thus mathematical) reasoning heavily relies on
;;; a simple albeit powerful law: <<<modus ponens|||(lambda (x) t)>>>
;; (a.k.a. detachment, cut, resolution, etc.)

;;; ### if we know that "A implies B"
;;; ### and if it is the case that "A holds"
;;; ### ... then we can deduce that "B holds" also.

(check-type? [A :type] [B :type]

   (lambda [f (==> A B)]
     (lambda [x A]
       (f x)))

   ;; of type
   
   (==> (==> A B) A
        B))

;; Modus ponens is beta-reduction (function application) it is that simple!



;;; # Universal quantifier

;;; In type theory, the modus ponens naturally generalizes
;;; to instantiation of universal quantifiers.

(check-type?
 [Thing :type] [man (==> Thing :type)] [mortal (==> Thing :type)]
 [socrate Thing]

 (lambda [H1 (forall [t Thing] (==> (man t) (mortal t)))]
   (lambda [H2 (man socrate)]
     ((H1 socrate) H2)))
 
 ;; Was Aristotle right?
 
 (==> (forall [t Thing]
        (==> (man t) (mortal t)))
      (man socrate)
      ;; thus
      (mortal socrate)))




;;; # Our first (low-level) proof ...

;;; Let's try to prove something about
;;; the implication and conjunction

(check-type? [A :type] [B :type] [C :type]
             
  (lambda [H1 (==> A B)]
    (lambda [H2 (and C A)]
      (H1 ((H2 A) (lambda [x C] (lambda [y A] y))))))

  ;; ^^^ the proof term ^^^
  
  (==> (==> A B) (and C A)
       B))

;; This is the <<<Proof-as-Term|||t>>> part
;;     of the <<<Curry-Howard correspondance|||t>>> 



;;; # Entracte ...

;;; ## What we learned thus far ...

;;; ... that thanks to the Curry-Howard correspondence
;;; a lambda-calculus with types may be used to:

;;;   1) express logical propositions as types

;;;   2) formalise proofs of the propositions as terms carrying those types

;;; ## However ...

;;;   working directly with proofs-as-terms is cumbersome

;;;   and better <<<abstractions|||t>>> are required

;;; ---> so let's introduce the <<<LaTTe proof assistant|||t>>>



;;; # The LaTTe proof assistant

;;; ## Proof assistant
;;; a tool that allows to describe mathematical content on
;;; a computer, and assists in the mathematician's routine: proving things!


;;; ## About LaTTe
;;; LaTTe is a proof assistant implemented as a Clojure library
;;; with top-level forms for axioms, definitions, theorems and proofs.
;; available on Clojars: <<<[latte "0.3.2-SNAPSHOT"]|||(lambda (x) t)>>>

;;; ## Notable features

;;; - any Clojure Development environment can be used to do maths!
;; (e.g. I use both Cider and Gorilla Repl, sometimes together via nrepl...)

;;; - it leverages the Clojure (JVM/Maven) ecosystem for <<<proving in the large|||t>>>

;;; - it supports a DSL for declarative proof scripts <<<<-- hot!|||t>>>



;;; # Our first LaTTe theorems ...

(defthm and-elim-right
  "Right elimination for conjunction."
  [[A :type] [B :type]]
  (==> (and A B)
       B))

;; Warning: proof required !
(proof and-elim-right
    :term
  (lambda [H (and A B)]
    ((H B) (lambda [x A] (lambda [y B] y)))))

(defthm dummy-theorem
  "This is an example theorem"
  [[A :type] [B :type] [C :type]]
  (==> (==> A B) (and C A)
       B))

(proof dummy-theorem
    :script
  (assume [H1 (==> A B)
           H2 (and C A)]
    (have a A :by ((and-elim-right C A) H2))
    (have b B :by (H1 a))
    (qed b)))
;;    :term
;;  (lambda [H1 (==> A B)]
;;    (lambda [H2 (and C A)]
;;      (H1 ((and-elim-right C A) H2)))))



;;; # Let's do some real maths...

;;; ### Our objective (in the few minutes to come):

;;; 1) live-code in LaTTe the <<<Peano arithmetics|||t>>>
;;; for natural numbers, and

;;; 2) demontrate an important inductive property
;;; about them...



;;; # The Peano arithmetics
;;; ### in (a bunch of) blinks of an eye

(defaxiom nat
  "The first Peano primive: ℕ is a primitive set"
  []
  :type)

(defaxiom zero
  "The second Peano primitive: 0 is in ℕ"
  []
  nat)

(defaxiom succ
"The third Peano primitive: the successor function of type ℕ ⟶ ℕ"
  []
  (==> nat nat))

(defaxiom nat-zero
  "The first Peano axiom: there is no successor in ℕ that equals 0"
  []
  (forall [n nat]
    (not (equal nat (succ n) zero))))

(defaxiom nat-succ-inj
  "The second Peano axiom: the successor function is injective"
  []
  (forall [n m nat]
    (==> (equal nat (succ n) (succ m))
         (equal nat n m))))

(defaxiom nat-induct
  "The third Peano axiom: induction principle on ℕ"
  [[P (==> nat :type)]]
  (==> (P zero)
       (forall [k nat]
         (==> (P k) (P (succ k))))
       (forall [n nat] (P n))))



               


;;; # A proof by induction

(defthm nat-case
  "Proof by case analysis."
  [[P (==> nat :type)]]
  (==> (P zero)
       (forall [k nat] (P (succ k)))
       (forall [n nat] (P n))))

(proof nat-case
  :script
  "First we state our assumptions."     
  (assume [Hz (P zero)
           HS (forall [k nat] (P (succ k)))]
          "Now we proceed by induction on n."
          "base case (n=0): trivial since (P zero) by Hz"
          "inductive case. Suppose (P k) for some natural number k"
    (assume [k nat
             Hind (P k)]
            "Let's prove that (P (succ k))"
            (have a (P (succ k)) :by (HS k))
            "Hence for any k (==> (P k) (P succ k))"
      (have b (forall [k nat]
                (==> (P k) (P (succ k))))
            :discharge [k Hind a]))
    "Thus (P n) is true for any n thanks to nat-induct."
    (have c (forall [n nat] (P n))
          :by ((nat-induct P) Hz b))
    (qed c)))

(definition nat-split
  "The split of natural numbers."
  [[n nat]]
  (or (equal nat n zero)
      (exists [m nat]
        (equal nat n (succ m)))))
  
(defthm nat-strong
  "A natural integer is either zero or the successor of
another integer"
  []
  (forall [n nat]
    (nat-split n)))
    
(proof nat-strong
    :script
  "We do the proof by case analysis on n."
  "1) case n=0"
  "0 = 0  by reflexivity"
  (have a1 (equal nat zero zero)
        :by (eq/eq-refl nat zero))
  "hence the base case."
  (have a (nat-split zero)
        :by ((p/or-intro-left
              (equal nat zero zero)
              (exists [m nat]
                (equal nat zero (succ m)))) a1))
  "2) case n=k+1 for an arbitrary k"
  (assume [k nat]
    "Let the predicate Q(m) such that k+1=m+1"
    (have Q _ :by (lambda [m nat] (equal nat (succ k) (succ m))))
    "Since  k+1 = k+1 by reflexivity we know that Q(k) is true."
    (have b1 (Q k)
          :by (eq/eq-refl nat (succ k)))
    "hence there exists an m such that k+1=m+1 (namely k)"
    (have b2 (exists [m nat]
                    (equal nat (succ k) (succ m)))
          :by ((q/ex-intro nat Q k) b1))
    "from this we get that P(k+1) is true."
    (have b3 (nat-split (succ k))
          :by ((p/or-intro-right (equal nat (succ k) zero)
                                 (exists [m nat]
                                   (equal nat (succ k) (succ m)))) b2))
    "hence we can deduce the case for k+1 from the case of k."
    (have b (forall [k nat]
              (nat-split (succ k)))
          :discharge [k b3]))
  "we can conclude by applying the case analysis theorem."
  (have concl _
        :by ((nat-case nat-split) a ))
  (qed concl))
    


;;; # Yes, we could!
;;; (I hope you enjoyed the ride...)

;;; ### Mathematics can be fun, (almost) as fun as live-coding in Clojure!
;; but ... wait! this *is* live-coding in Clojure!

;;; Formalizing and proving things can be a very addictive <<<puzzle game|||(lambda (x) t)>>>
;;; - with both a <<<single player mode|||(lambda (x) t)>>> and <<<multiplayer cooperation|||(lambda (x) t)>>> available!
;; (MMO being considered)

;;; - An un limited number of puzzles awaits you:

;;;    * starters: propositional logic, basic quantifiers, etc.

;;;    * serious challenges: numbers, inductive types, recursive functions, etc.
;;    (way better than Sudoku and even kakuro)

;;;    * professional-grade puzzles: modern mathematics



;;; # ... and what about a real challenge?

(defthm life-universe-rest
  "" [[Algos :type] [P Algos] [NP Algos]]
  (not (equal Algos P NP))) ;; or is-it?

(proof life-universe-rest
    :script
  "TODO")

;;;
;;; ### Let's play together at: https://github.com/fredokun/LaTTe
;;; you're just a `lein new my-cool-maths` away...
;; (no? really? how unfortunate :~( )


;;; # Thank you!
