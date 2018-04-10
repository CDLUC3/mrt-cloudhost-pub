# If you want to have in master exactly the same files state as in other_branch and save history
git branch
git checkout master
git checkout https
git add --all
git commit -m "* copy https to master working tree"
git clean -fd

