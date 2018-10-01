# TP1 - Infonuagique

Afin d'executer ce TP, veuillez proceder avec les etapes suivantes:

**Envoyer le travail sur la machine virtuelle:**
```
 scp -i sshkeys.pem 1762064-1721384-TP1-INF8480.tar.gz ubuntu@ip-flottante
```
Notez que l'ip flottante utilisee dans ce TP etait: 132.207.12.114. 
Vous pouvez la reutiliser.

Juste au cas, nous avons laissez le fichier sshkeys.pem donc le charge
peut lutiliser pour les cles privees. Par contre, sil a dautre cles ssh, il peut simplement remplacer largument sshkeys.pem par un autre pour la cle prive.

**Installation du serveur sur la machine virtuelle:**
Se connecter a la machine virtuelle en effectuant la commande suivante: 

```
 ssh -i sshkeys.pem ubuntu@ip-flottante
```
Si l'ip flotante est differente ici, il faut simplement la mettre en argument en remplacant lautre. 

Dezipper le fichier envoye sur la VM: 
```
 tar xvzf 1762064-1721384-TP1-INF8480.tar.gz
```

Se rendre dans le dossier bin:
```
 cd 1762064-1721384-TP1-INF8480/Partie2/ResponseTime_Analyzer/bin
```

Demarrer le registre RMI:
```
 rmiregistry
```

Ouvrir un autre terminal, et se connecter avec la machine virtuelle (avec la meme commande que precedemment):
```
 ssh -i sshkeys.pem ubuntu@ip-flottante
```

Executer le serveur
```
  1762064-1721384-TP1-INF8480/Partie2/ResponseTime_Analyzer/server.sh
```

**Installation du client sur la machine locale:**
- Se rendre dans le dossier approprie:
```
 cd 1762064-1721384-TP1-INF8480/Partie2/ResponseTime_Analyzer/
```

- Entrer les commandes du TP par exemple : 
```
 ./client.sh new testuser testmp

 ./client.sh list
```

Il est egalement possible d'installer plusieurs clients dans des dossiers differents afin de voir si le serveur est bien capable de repondre aux demandes de plus d'un client (ce qui est le cas).


Veuillez noter qu'il est preferable d'executer le client sur un ordinateur de l'ecole, soit un ordinateur
utilisant JAVA 1.8. 